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
    private static final String PRIMARY_ALT_KEY_COLUMN = "primaryAltKeyColumn";
    private static final String PRIMARY_DATA_COLUMN = "primaryDataColumn";
    private static final String PRIMARY_PATH = "primaryPath";

    private static final String ASSOC_DATA_TYPE = "assocDataType";
    private static final String ASSOC_LEFT_COLUMN = "assocLeftColumn";
    private static final String ASSOC_RIGHT_COLUMN = "assocRightColumn";
    private static final String ASSOC_PATH = "assocPath";

    private static final String INDIRECT_RELATED_DATA_TYPE = "indirectRelatedDataType";
    private static final String INDIRECT_RELATED_KEY_COLUMN = "indirectRelatedKeyColumn";
    private static final String INDIRECT_RELATED_DATA_COLUMN = "indirectRelatedDataColumn";
    private static final String INDIRECT_RELATED_PATH = "indirectRelatedPath";

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
            DataTypes.createStructField(PRIMARY_ALT_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(PRIMARY_DATA_COLUMN, DataTypes.StringType, false)
    ));
    private final StructType primaryOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(PRIMARY_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(PRIMARY_ALT_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(PRIMARY_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false)
    ));

    private final StructType directRelatedInputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));
    private final StructType directRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
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
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final Join directJoin = newOneToMany(
            DIRECT_RELATED_DATA_TYPE,
            DIRECT_RELATED_KEY_COLUMN
    );

    private final Join directJoinWithAltKey = newOneToMany(
            PRIMARY_ALT_KEY_COLUMN,
            DIRECT_RELATED_DATA_TYPE,
            DIRECT_RELATED_KEY_COLUMN
    );

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
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                null
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE))),
                new Tuple2<>(2, Arrays.asList(newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE))),
                new Tuple2<>(3, Arrays.asList(newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE))),
                new Tuple2<>(4, Arrays.asList(newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE)))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_oneToManyJoin() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4")
        );

        Dataset<Row> directRelatedDataset = newDataset(
                directRelatedInputSchema,
                RowFactory.create(1, "direct11"),
                RowFactory.create(1, "direct12"),
                RowFactory.create(1, "direct13"),
                RowFactory.create(2, "direct21"),
                RowFactory.create(2, "direct22"),
                RowFactory.create(3, "direct31"),
                RowFactory.create(98, "unrelated1"),
                RowFactory.create(99, "unrelated2")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(directJoin)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 1, "direct11", DIRECT_RELATED_DATA_TYPE, 1),
                        newRow(directRelatedOutputSchema, 1, "direct12", DIRECT_RELATED_DATA_TYPE, 1),
                        newRow(directRelatedOutputSchema, 1, "direct13", DIRECT_RELATED_DATA_TYPE, 1)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 2, "direct21", DIRECT_RELATED_DATA_TYPE, 2),
                        newRow(directRelatedOutputSchema, 2, "direct22", DIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 3, "direct31", DIRECT_RELATED_DATA_TYPE, 3)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE)
                ))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_oneToManyJoinOnAltKey() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 10, "primary1"),
                RowFactory.create(2, 20, "primary2"),
                RowFactory.create(3, 20, "primary3"),
                RowFactory.create(4, 30, "primary4"),
                RowFactory.create(5, 40, "primary5")
        );

        Dataset<Row> directRelatedDataset = newDataset(
                directRelatedInputSchema,
                RowFactory.create(10, "direct101"),
                RowFactory.create(20, "direct201"),
                RowFactory.create(20, "direct202"),
                RowFactory.create(30, "direct301"),
                RowFactory.create(30, "direct302"),
                RowFactory.create(98, "unrelated1"),
                RowFactory.create(99, "unrelated2")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(directJoinWithAltKey)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 10, "primary1", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 10, "direct101", DIRECT_RELATED_DATA_TYPE, 1)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 20, "primary2", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 20, "direct201", DIRECT_RELATED_DATA_TYPE, 2),
                        newRow(directRelatedOutputSchema, 20, "direct202", DIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 20, "primary3", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 20, "direct201", DIRECT_RELATED_DATA_TYPE, 3),
                        newRow(directRelatedOutputSchema, 20, "direct202", DIRECT_RELATED_DATA_TYPE, 3)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 30, "primary4", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 30, "direct301", DIRECT_RELATED_DATA_TYPE, 4),
                        newRow(directRelatedOutputSchema, 30, "direct302", DIRECT_RELATED_DATA_TYPE, 4)
                )),
                new Tuple2<>(5, Arrays.asList(
                        newRow(primaryOutputSchema, 5, 40, "primary5", PRIMARY_DATA_TYPE)
                ))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_manyToManyJoin() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4")
        );

        Dataset<Row> assocDataset = newDataset(
                assocInputSchema,
                RowFactory.create(2, 21),
                RowFactory.create(3, 31),
                RowFactory.create(3, 32),
                RowFactory.create(4, 32),
                RowFactory.create(4, 41),
                RowFactory.create(4, 42),
                RowFactory.create(4, 43),
                RowFactory.create(98, 198),
                RowFactory.create(99, 199)
        );

        Dataset<Row> indirectRelatedDataset = newDataset(
                indirectRelatedInputSchema,
                RowFactory.create(21, "indirect21"),
                RowFactory.create(31, "indirect31"),
                RowFactory.create(32, "indirect32"),
                RowFactory.create(41, "indirect41"),
                RowFactory.create(42, "indirect42"),
                RowFactory.create(43, "indirect43"),
                RowFactory.create(198, "unrelated1"),
                RowFactory.create(199, "unrelated2")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(indirectJoin)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 21, "indirect21", INDIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 31, "indirect31", INDIRECT_RELATED_DATA_TYPE, 3),
                        newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 3)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 4),
                        newRow(indirectRelatedOutputSchema, 41, "indirect41", INDIRECT_RELATED_DATA_TYPE, 4),
                        newRow(indirectRelatedOutputSchema, 42, "indirect42", INDIRECT_RELATED_DATA_TYPE, 4),
                        newRow(indirectRelatedOutputSchema, 43, "indirect43", INDIRECT_RELATED_DATA_TYPE, 4)
                ))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_multipleJoin() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4"),
                RowFactory.create(5, 5, "primary5")
        );

        Dataset<Row> directRelatedDataset = newDataset(
                directRelatedInputSchema,
                RowFactory.create(1, "direct11"),
                RowFactory.create(1, "direct12"),
                RowFactory.create(1, "direct13"),
                RowFactory.create(2, "direct21"),
                RowFactory.create(2, "direct22"),
                RowFactory.create(3, "direct31"),
                RowFactory.create(98, "unrelated1"),
                RowFactory.create(99, "unrelated2")
        );

        Dataset<Row> assocDataset = newDataset(
                assocInputSchema,
                RowFactory.create(2, 21),
                RowFactory.create(3, 31),
                RowFactory.create(3, 32),
                RowFactory.create(4, 41),
                RowFactory.create(4, 42),
                RowFactory.create(4, 43),
                RowFactory.create(98, 198),
                RowFactory.create(99, 199)
        );

        Dataset<Row> indirectRelatedDataset = newDataset(
                indirectRelatedInputSchema,
                RowFactory.create(21, "indirect21"),
                RowFactory.create(31, "indirect31"),
                RowFactory.create(32, "indirect32"),
                RowFactory.create(41, "indirect41"),
                RowFactory.create(42, "indirect42"),
                RowFactory.create(43, "indirect43"),
                RowFactory.create(198, "unrelated1"),
                RowFactory.create(199, "unrelated2")
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Arrays.asList(directJoin, indirectJoin)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 1, "direct11", DIRECT_RELATED_DATA_TYPE, 1),
                        newRow(directRelatedOutputSchema, 1, "direct12", DIRECT_RELATED_DATA_TYPE, 1),
                        newRow(directRelatedOutputSchema, 1, "direct13", DIRECT_RELATED_DATA_TYPE, 1)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 2, "direct21", DIRECT_RELATED_DATA_TYPE, 2),
                        newRow(directRelatedOutputSchema, 2, "direct22", DIRECT_RELATED_DATA_TYPE, 2),
                        newRow(indirectRelatedOutputSchema, 21, "indirect21", INDIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 3, "direct31", DIRECT_RELATED_DATA_TYPE, 3),
                        newRow(indirectRelatedOutputSchema, 31, "indirect31", INDIRECT_RELATED_DATA_TYPE, 3),
                        newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 3)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchema, 41, "indirect41", INDIRECT_RELATED_DATA_TYPE, 4),
                        newRow(indirectRelatedOutputSchema, 42, "indirect42", INDIRECT_RELATED_DATA_TYPE, 4),
                        newRow(indirectRelatedOutputSchema, 43, "indirect43", INDIRECT_RELATED_DATA_TYPE, 4)
                )),
                new Tuple2<>(5, Arrays.asList(
                        newRow(primaryOutputSchema, 5, 5, "primary5", PRIMARY_DATA_TYPE)
                ))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_unexpectedJoinType() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1")
        );

        Dataset<Row> directRelatedDataset = newDataset(
                directRelatedInputSchema
        );

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever);

        Join customJoin = new Join() { };
        customJoin.setRelatedDataType(DIRECT_RELATED_DATA_TYPE);
        customJoin.setRelatedKeyColumn(DIRECT_RELATED_KEY_COLUMN);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(customJoin)
        );
        expectException("Unexpected Join Type", () -> contextRetriever.retrieveContext(contextDefinition));
    }

    @Test
    public void retrieveContext_noPathForDataType() {
        Map<String, String> noPaths = Collections.emptyMap();
        Map<String, Dataset<Row>> noDatasets = Collections.emptyMap();

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(noDatasets);
        ContextRetriever contextRetriever = new ContextRetriever(noPaths, datasetRetriever);

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.emptyList()
        );
        expectException("No path mapping found for datatype", () -> contextRetriever.retrieveContext(contextDefinition));
    }

    private void assertOutput(List<Tuple2<Object, List<Row>>> expected, List<Tuple2<Object, List<Row>>> actual) {
        // Sort the `actual` outer list.
        // Even with one task, Spark likes to jumble the order of the results.
        // There may be a need to sort the inner Row lists in the future, but there is no need right now.
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

    private void expectException(String exceptionPrefix, Runnable task) {
        boolean passed = false;
        try {
            task.run();
        }
        catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith(exceptionPrefix)) {
                passed = true;
            }
            else {
                throw e;
            }
        }

        Assert.assertTrue("Exception not encountered", passed);
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

    private OneToMany newOneToMany(String relatedDataType, String relatedColumn) {
        return newOneToMany(null, relatedDataType, relatedColumn);
    }

    private OneToMany newOneToMany(String primaryColumn, String relatedDataType, String relatedColumn) {
        OneToMany retVal = new OneToMany();

        retVal.setPrimaryDataTypeColumn(primaryColumn);
        retVal.setRelatedDataType(relatedDataType);
        retVal.setRelatedKeyColumn(relatedColumn);

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
