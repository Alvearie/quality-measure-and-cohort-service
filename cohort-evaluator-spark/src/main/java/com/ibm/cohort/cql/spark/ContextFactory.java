/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.Join;
import com.ibm.cohort.cql.spark.aggregation.ManyToMany;
import com.ibm.cohort.cql.spark.aggregation.OneToMany;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextFactory {

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
        ContextFactory factory = new ContextFactory(spark, inputPaths);

        ObjectMapper mapper = new ObjectMapper();
        File contextFile = new File("/Users/dkwasny/ScratchSpace/joinstuff/test-inputs/contexts.json");
        ContextDefinition contextDefinition = mapper.readValue(contextFile, ContextDefinition.class);

        JavaPairRDD<Object, Row> allData = factory.readContext(contextDefinition);
        allData.collect().forEach(System.out::println);
    }

    private final SparkSession spark;
    private final Map<String, String> inputPaths;

    public ContextFactory(SparkSession spark, Map<String, String> inputPaths) {
        this.spark = spark;
        this.inputPaths = inputPaths;
    }

    public JavaPairRDD<Object, Row> readContext(ContextDefinition contextDefinition) {
        List<Dataset<Row>> datasets = new ArrayList<>();

        String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();
        String primaryDataType = contextDefinition.getPrimaryDataType();
        Dataset<Row> primaryDataset = readDataset(primaryDataType);

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

        JavaPairRDD<Object, Row> retVal = toPairRDD(primaryDataset, primaryKeyColumn);
        for (Dataset<Row> dataset : datasets) {
            retVal = retVal.union(toPairRDD(dataset, primaryKeyColumn));
        }

        return retVal;
    }

    private JavaPairRDD<Object, Row> toPairRDD(Dataset<Row> dataset, String contextColumn) {
        return dataset.javaRDD().mapToPair(row -> {
            Object joinValue = row.getAs(contextColumn);
            return new Tuple2<>(joinValue, row);
        });
    }

    private Dataset<Row> readDataset(String dataType) {
        // TODO: Fail on missing path
        String path = inputPaths.get(dataType);
        return spark.read()
                // TODO: Parquet
                .option("header", true)
                .option("inferSchema", true)
                .csv(path)
                .withColumn(SparkCqlEvaluator.SOURCE_FACT_IDX, functions.lit(dataType));
    }

}
