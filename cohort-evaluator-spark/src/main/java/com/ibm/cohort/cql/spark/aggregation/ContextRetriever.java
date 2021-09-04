/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.spark.data.CsvDatasetRetriever;
import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.spark.SparkCqlEvaluator;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import scala.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Javadoc all the things
public class ContextRetriever {

    public static final String SOURCE_FACT_IDX = "__SOURCE_FACT";

    // TODO: Remove once unit tests are spun up
    public static void main(String[] args) throws Exception {
        Map<String, String> inputPaths = new HashMap<>();
        inputPaths.put("Primary", "/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/primary.csv");
        inputPaths.put("Related", "/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/related.csv");
        inputPaths.put("Assoc", "/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/assoc.csv");
        inputPaths.put("AssocOther", "/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/assoc-other.csv");
        inputPaths.put("AssocRelated", "/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/assoc-related.csv");

        SparkSession spark = SparkSession.builder()
                .appName("Local Application")
                .master("local[1]")
                .config("spark.sql.shuffle.partitions", 10)
                .getOrCreate();
        DatasetRetriever datasetRetriever = new CsvDatasetRetriever(spark);
        ContextRetriever factory = new ContextRetriever(inputPaths, datasetRetriever);

        ObjectMapper mapper = new ObjectMapper();
        File contextFile = new File("/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/contexts.json");
        ContextDefinition contextDefinition = mapper.readValue(contextFile, ContextDefinition.class);

        JavaPairRDD<Object, List<Row>> allData = factory.retrieveContext(contextDefinition);
        allData.collect().forEach(System.out::println);
    }

    private final Map<String, String> inputPaths;
    private final DatasetRetriever datasetRetriever;

    public ContextRetriever(Map<String, String> inputPaths, DatasetRetriever datasetRetriever) {
        this.inputPaths = inputPaths;
        this.datasetRetriever = datasetRetriever;
    }

    public JavaPairRDD<Object, List<Row>> retrieveContext(ContextDefinition contextDefinition) {
        List<Dataset<Row>> datasets = gatherDatasets(contextDefinition);

        String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();
        List<JavaPairRDD<Object, Row>> rddList = datasets.stream()
                .map(x -> toPairRDD(x, primaryKeyColumn))
                .collect(Collectors.toList());

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

    private List<Dataset<Row>> gatherDatasets(ContextDefinition contextDefinition) {
        List<Dataset<Row>> datasets = new ArrayList<>();

        String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();
        String primaryDataType = contextDefinition.getPrimaryDataType();
        Dataset<Row> primaryDataset = readDataset(primaryDataType);
        datasets.add(primaryDataset);

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
            }
            else if (join.getClass() == ManyToMany.class) {
                ManyToMany manyToMany = (ManyToMany)join;
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
            }
            else {
                throw new RuntimeException("Unexpected Join Type: " + join.getClass().getName());
            }

            // Somewhat chunky due to Spark expecting Arrays instead of Collections
            Column[] relatedColumns = Arrays.stream(relatedDataset.columns())
                    .map(relatedDataset::col)
                    .toArray(Column[]::new);
            List<Column> allColumns = new ArrayList<>();
            allColumns.add(primaryDataset.col(primaryJoinColumn));
            allColumns.addAll(Arrays.asList(relatedColumns));
            Column[] selectColumns = allColumns.toArray(new Column[0]);

            joinedDataset = joinedDataset.select(selectColumns);

            datasets.add(joinedDataset);
        }

        return datasets;
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
