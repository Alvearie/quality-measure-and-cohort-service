/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Javadoc all the things
public class ContextRetriever {

    public static final String SOURCE_FACT_IDX = "__SOURCE_FACT";
    public static final String JOIN_CONTEXT_VALUE_IDX = "__JOIN_CONTEXT_VALUE";

    private final Map<String, String> inputPaths;
    private final DatasetRetriever datasetRetriever;

    public ContextRetriever(Map<String, String> inputPaths, DatasetRetriever datasetRetriever) {
        this.inputPaths = inputPaths;
        this.datasetRetriever = datasetRetriever;
    }

    public JavaPairRDD<Object, List<Row>> retrieveContext(ContextDefinition contextDefinition) {
        // TODO: What about grouping first and then joining the rdds together?
        // That might cause less serialization.
        List<JavaPairRDD<Object, Row>> rddList = gatherRDDs(contextDefinition);

        JavaPairRDD<Object, Row> allData = unionPairRDDs(rddList);

        JavaPairRDD<Object, List<Row>> retVal;
        boolean groupContext = contextDefinition.getRelationships() != null
                && contextDefinition.getRelationships().size() > 0;
        if (groupContext) {
            retVal = groupPairRDDs(allData);
        }
        else {
            // If no actual relationships are defined, then create a
            // single record context for the primary row.
            retVal = allData.mapToPair(
                    (tuple2) -> new Tuple2<>(tuple2._1(), Collections.singletonList(tuple2._2()))
            );
        }

        return retVal;
    }

    private List<JavaPairRDD<Object, Row>> gatherRDDs(ContextDefinition contextDefinition) {
        List<JavaPairRDD<Object, Row>> retVal = new ArrayList<>();

        String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();
        String primaryDataType = contextDefinition.getPrimaryDataType();
        Dataset<Row> primaryDataset = readDataset(primaryDataType);
        retVal.add(toPairRDD(primaryDataset, primaryKeyColumn));

        // We need to retain the original context value from the primary datatype.
        // This is done by adding a "placeholder column" that selects the context value.
        // This is needed when we retain only the columns from the related dataset.
        Column joinContextColumn = primaryDataset.col(primaryKeyColumn).as(JOIN_CONTEXT_VALUE_IDX);

        if (contextDefinition.getRelationships() != null) {
            for (Join join : contextDefinition.getRelationships()) {
                String primaryJoinColumn = join.getPrimaryDataTypeColumn() == null
                        ? primaryKeyColumn
                        : join.getPrimaryDataTypeColumn();
                String relatedDataType = join.getRelatedDataType();
                String relatedColumnName = join.getRelatedKeyColumn();
                Dataset<Row> relatedDataset = readDataset(relatedDataType);
                Dataset<Row> joinedDataset;

                if (join.getClass() == OneToMany.class) {
                    Column joinCriteria = primaryDataset.col(primaryJoinColumn)
                            .equalTo(relatedDataset.col(relatedColumnName));
                    joinedDataset = primaryDataset.join(relatedDataset, joinCriteria);
                } else if (join.getClass() == ManyToMany.class) {
                    ManyToMany manyToMany = (ManyToMany) join;
                    String assocDataType = manyToMany.getAssociationDataType();
                    Dataset<Row> assocDataset = readDataset(assocDataType);
                    // TODO: Address naming.
                    // I think something simple like "left" and "right" would be easier
                    // to understand.
                    String assocPrimaryColumnName = manyToMany.getAssociationOneKeyColumn();
                    String assocRelatedColumnName = manyToMany.getAssociationManyKeyColumn();

                    Column primaryJoinCriteria = primaryDataset.col(primaryJoinColumn)
                            .equalTo(assocDataset.col(assocPrimaryColumnName));
                    joinedDataset = primaryDataset.join(assocDataset, primaryJoinCriteria);

                    Column relatedJoinCriteria = joinedDataset.col(assocRelatedColumnName)
                            .equalTo(relatedDataset.col(relatedColumnName));
                    joinedDataset = joinedDataset.join(relatedDataset, relatedJoinCriteria);
                } else {
                    throw new RuntimeException("Unexpected Join Type: " + join.getClass().getName());
                }

                List<Column> retainedColumns = Arrays.stream(relatedDataset.columns())
                        .map(relatedDataset::col)
                        .collect(Collectors.toCollection(ArrayList::new));
                retainedColumns.add(joinContextColumn);
                Column[] columnArray = retainedColumns.toArray(new Column[0]);
                joinedDataset = joinedDataset.select(columnArray);

                retVal.add(toPairRDD(joinedDataset, JOIN_CONTEXT_VALUE_IDX));
            }
        }

        return retVal;
    }

    /**
     * Given a set of rows that are indexed by context value, reorganize the data so
     * that all rows related to the same context are grouped into a single pair.
     *
     * @param allData           rows mapped from context value to a single data row
     * @return rows grouped mapped from context value to a list of all data for that
     *         context
     */
    private JavaPairRDD<Object, List<Row>> groupPairRDDs(JavaPairRDD<Object, Row> allData) {
        // Regroup data by context ID so that all input data for the same
        // context is represented as a single key mapped to a list of rows
        return allData.combineByKey(create -> {
            List<Row> dataRowList = new ArrayList<>();
            dataRowList.add(create);
            return dataRowList;
        }, (list, val) -> {
            list.add(val);
            return list;
        }, (list1, list2) -> {
            List<Row> dataRowList = new ArrayList<>(list1);
            dataRowList.addAll(list2);
            return dataRowList;
        });
    }

    private JavaPairRDD<Object, Row> toPairRDD(Dataset<Row> dataset, String contextColumn) {
        return dataset.javaRDD().mapToPair(row -> {
            Object joinValue = row.getAs(contextColumn);
            return new Tuple2<>(joinValue, row);
        });
    }

    private JavaPairRDD<Object, Row> unionPairRDDs(List<JavaPairRDD<Object, Row>> rddList) {
        JavaPairRDD<Object, Row> retVal = null;
        for (JavaPairRDD<Object, Row> rdd : rddList) {
            if (retVal == null) {
                retVal = rdd;
            }
            else {
                retVal = retVal.union(rdd);
            }
        }
        return retVal;
    }

    private Dataset<Row> readDataset(String dataType) {
        String path = inputPaths.get(dataType);
        if (path == null) {
            throw new IllegalArgumentException(String.format("No path mapping found for datatype %s", dataType));
        }
        return datasetRetriever.readDataset(path)
                .withColumn(SOURCE_FACT_IDX, functions.lit(dataType));
    }

}
