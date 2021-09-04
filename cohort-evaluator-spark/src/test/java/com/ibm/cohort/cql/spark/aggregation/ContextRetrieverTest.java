/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.spark.data.TestDatasetRetriever;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextRetrieverTest {

    private static final String PRIMARY_NAME = "primaryName";
    private static final String PRIMARY_DATA_TYPE = "primaryDataType";
    private static final String PRIMARY_KEY_COLUMN = "primaryKeyColumn";
    private static final String PRIMARY_DATA_COLUMN = "primaryDataColumn";
    private static final String PRIMARY_PATH = "primaryPath";

    private static final String ASSOC_NAME = "assocName";
    private static final String ASSOC_DATA_TYPE = "assocDataType";
    private static final String ASSOC_LEFT_COLUMN = "assocLeftColumn";
    private static final String ASSOC_RIGHT_COLUMN = "assocRightColumn";
    private static final String ASSOC_PATH = "assocPath";

    private static final String INDIRECT_RELATED_NAME = "indirectRelatedName";
    private static final String INDIRECT_RELATED_DATA_TYPE = "indirectRelatedDataType";
    private static final String INDIRECT_RELATED_KEY_COLUMN = "indirectRelatedKeyColumn";
    private static final String INDIRECT_RELATED_DATA_COLUMN = "indirectRelatedDataColumn";
    private static final String INDIRECT_RELATED_PATH = "indirectRelatedPath";

    private static final String DIRECT_RELATED_NAME = "directRelatedName";
    private static final String DIRECT_RELATED_DATA_TYPE = "directRelatedDataType";
    private static final String DIRECT_RELATED_KEY_COLUMN = "directRelatedKeyColumn";
    private static final String DIRECT_RELATED_DATA_COLUMN = "directRelatedDataColumn";
    private static final String DIRECT_RELATED_PATH = "directRelatedPath";
    
    private static SparkSession spark;

    @BeforeClass
    public static void initialize() {
        spark = SparkSession.builder()
                .master("local[1]")
                .config("spark.sql.shuffle.partitions", 1)
                .getOrCreate();
    }

    @AfterClass
    public static void shutdown() {
        spark.stop();
        spark = null;
    }

    private final Map<String, String> inputPaths = new HashMap<String, String>(){{
        put(PRIMARY_DATA_TYPE, PRIMARY_PATH);
        put(ASSOC_DATA_TYPE, ASSOC_PATH);
        put(INDIRECT_RELATED_DATA_TYPE, INDIRECT_RELATED_PATH);
        put(DIRECT_RELATED_DATA_TYPE, DIRECT_RELATED_PATH);
    }};

    private final StructType primaryInputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(PRIMARY_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(PRIMARY_DATA_COLUMN, DataTypes.StringType, false)
    ));
    private final StructType primaryOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(PRIMARY_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(PRIMARY_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false)
    ));

    private final StructType assocInputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(ASSOC_LEFT_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(ASSOC_RIGHT_COLUMN, DataTypes.IntegerType, false)
    ));

    private final StructType indirectRelatedInputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));
    private final StructType indirectRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(PRIMARY_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false)
    ));

    private final StructType directRelatedInputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));
    private final StructType directRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(PRIMARY_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false)
    ));

    private final Join indirectJoin = newManyToMany(
            INDIRECT_RELATED_DATA_TYPE,
            INDIRECT_RELATED_KEY_COLUMN,
            ASSOC_DATA_TYPE,
            ASSOC_LEFT_COLUMN,
            ASSOC_RIGHT_COLUMN
    );

    @Test
    public void retrieveContext_noJoins() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, "primary1"),
                RowFactory.create(2, "primary2"),
                RowFactory.create(3, "primary3")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefintion = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.emptyList()
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefintion).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(newRow(primaryOutputSchema, 1, "primary1", PRIMARY_DATA_TYPE))),
                new Tuple2<>(2, Arrays.asList(newRow(primaryOutputSchema, 2, "primary2", PRIMARY_DATA_TYPE))),
                new Tuple2<>(3, Arrays.asList(newRow(primaryOutputSchema, 3, "primary3", PRIMARY_DATA_TYPE)))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_manyToManyJoin() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, "primary1"),
                RowFactory.create(2, "primary2"),
                RowFactory.create(3, "primary3")
        );

        Dataset<Row> assocDataset = newDataset(
                assocInputSchema,
                RowFactory.create(1, 51),
                RowFactory.create(2, 52),
                RowFactory.create(2, 53),
                RowFactory.create(3, 54),
                RowFactory.create(3, 55),
                RowFactory.create(3, 56)
        );

        Dataset<Row> indirectRelatedDataset = newDataset(
                indirectRelatedInputSchema,
                RowFactory.create(51, "indirect51"),
                RowFactory.create(52, "indirect52"),
                RowFactory.create(53, "indirect53"),
                RowFactory.create(54, "indirect54"),
                RowFactory.create(55, "indirect55"),
                RowFactory.create(56, "indirect56")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefintion = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(indirectJoin)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefintion).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, "primary1", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 1, 51, "indirect51", INDIRECT_RELATED_DATA_TYPE)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 2, 52, "indirect52", INDIRECT_RELATED_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 2, 53, "indirect53", INDIRECT_RELATED_DATA_TYPE)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, "primary3", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 3, 54, "indirect54", INDIRECT_RELATED_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 3, 55, "indirect55", INDIRECT_RELATED_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 3, 56, "indirect56", INDIRECT_RELATED_DATA_TYPE)
                ))
        );

        assertOutput(expected, actual);
    }

    private void assertOutput(List<Tuple2<Object, List<Row>>> expected, List<Tuple2<Object, List<Row>>> actual) {
        // TODO: Need to rework this.

        // TODO: This is kinda gross, but we're kinda screwed by the Object type.
        // Maybe find something cleaner with less warnings...again...Object is screwing us.
        // Also sort the inner row lists.  It's not needed _right now_ but better safe than sorry.
        List<Tuple2<Object, List<Row>>> sortedActual = new ArrayList<>(actual);
        sortedActual.sort(Comparator.comparing(x -> (Comparable)x._1));

        // Row.equals() does not compare schemas.
        Assert.assertEquals("Data mismatch", expected, sortedActual);

        // Manually ensure that each row has the correct schema.
        // We can assume all lists are of equal length and in the correct
        // order due to the above assertion.
        for (int i = 0; i < expected.size(); i++) {
            List<Row> expectedRows = expected.get(i)._2();
            List<Row> actualRows = sortedActual.get(i)._2();
            for (int j = 0; j < expectedRows.size(); j++) {
                StructType expectedSchema = expectedRows.get(j).schema();
                StructType actualSchema = actualRows.get(j).schema();
                Assert.assertEquals("Schema mismatch", expectedSchema, actualSchema);
            }
        }
    }

    private Row newRow(StructType schema, Object... values) {
        return new GenericRowWithSchema(values, schema);
    }

    private Dataset<Row> newDataset(StructType schema, Row... rows) {
        return spark.createDataFrame(Arrays.asList(rows), schema);
    }

    private ContextDefinition newContextDefinition(String name, String dataType, String keyColumn, List<Join> relations) {
        ContextDefinition retVal = new ContextDefinition();
        retVal.setName(name);
        retVal.setPrimaryDataType(dataType);
        retVal.setPrimaryKeyColumn(keyColumn);
        retVal.setRelationships(relations);
        return retVal;
    }

    private ManyToMany newManyToMany(
            String relatedDataType,
            String relatedColumn,
            String assocDataType,
            String assocLeft,
            String assocRight
    ) {
        return newManyToMany(null, relatedDataType, relatedColumn, assocDataType, assocLeft, assocRight);
    }
    private ManyToMany newManyToMany(
            String primaryColumn,
            String relatedDataType,
            String relatedColumn,
            String assocDataType,
            String assocLeft,
            String assocRight
    ) {
        ManyToMany retVal = new ManyToMany();
        retVal.setPrimaryDataTypeColumn(primaryColumn);
        retVal.setRelatedDataType(relatedDataType);
        retVal.setRelatedKeyColumn(relatedColumn);
        retVal.setAssociationDataType(assocDataType);
        retVal.setAssociationOneKeyColumn(assocLeft);
        retVal.setAssociationManyKeyColumn(assocRight);
        return retVal;
    }

}
