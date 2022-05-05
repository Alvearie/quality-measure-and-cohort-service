/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.cohort.cql.spark.BaseSparkTest;
import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.spark.data.TestDatasetRetriever;

import scala.Tuple2;

@SuppressWarnings("serial")
public class ContextRetrieverTest extends BaseSparkTest {

    // The primary data
    private static final String PRIMARY_NAME = "primaryName";
    private static final String PRIMARY_DATA_TYPE = "primaryDataType";
    private static final String PRIMARY_KEY_COLUMN = "primaryKeyColumn";
    private static final String PRIMARY_ALT_KEY_COLUMN = "primaryAltKeyColumn";
    private static final String PRIMARY_DATA_COLUMN = "primaryDataColumn";
    private static final String PRIMARY_PATH = "primaryPath";

    // The association table used in a many to many join
    private static final String ASSOC_DATA_TYPE = "assocDataType";
    private static final String ASSOC_LEFT_COLUMN = "assocLeftColumn";
    private static final String ASSOC_RIGHT_COLUMN = "assocRightColumn";
    private static final String ASSOC_PATH = "assocPath";

    // The related table in a many to many join
    private static final String INDIRECT_RELATED_DATA_TYPE = "indirectRelatedDataType";
    private static final String INDIRECT_RELATED_KEY_COLUMN = "indirectRelatedKeyColumn";
    private static final String INDIRECT_RELATED_DATA_COLUMN = "indirectRelatedDataColumn";
    private static final String INDIRECT_RELATED_PATH = "indirectRelatedPath";
    private static final String INDIRECT_RELATED_FILTER_COLUMN = "indirectRelatedFilterColumn";

    // The secondary related table in a many to many join
    private static final String SECONDARY_INDIRECT_RELATED_DATA_TYPE = "secondaryIndirectRelatedDataType";
    private static final String SECONDARY_INDIRECT_RELATED_KEY_COLUMN = "secondaryIndirectRelatedKeyColumn";
    private static final String SECONDARY_INDIRECT_RELATED_DATA_COLUMN = "secondaryIndirectRelatedDataColumn";
    private static final String SECONDARY_INDIRECT_RELATED_PATH = "secondaryIndirectRelatedPath";

    // The tertiary related table in a many to many join
    private static final String TERTIARY_INDIRECT_RELATED_DATA_TYPE = "tertiaryIndirectRelatedDataType";
    private static final String TERTIARY_INDIRECT_RELATED_KEY_COLUMN = "tertiaryIndirectRelatedKeyColumn";
    private static final String TERTIARY_INDIRECT_RELATED_DATA_COLUMN = "tertiaryIndirectRelatedDataColumn";
    private static final String TERTIARY_INDIRECT_RELATED_PATH = "tertiaryIndirectRelatedPath";

    // The related table in a one to many join
    private static final String DIRECT_RELATED_DATA_TYPE = "directRelatedDataType";
    private static final String DIRECT_RELATED_KEY_COLUMN = "directRelatedKeyColumn";
    private static final String DIRECT_RELATED_DATA_COLUMN = "directRelatedDataColumn";
    private static final String DIRECT_RELATED_FILTER_COLUMN = "directRelatedFilterColumn";
    private static final String DIRECT_RELATED_PATH = "directRelatedPath";
    
    private static SparkSession spark;

    @BeforeClass
    public static void initialize() {
        spark = initializeSession();
    }

    private final Map<String, String> inputPaths = new HashMap<String, String>(){{
        put(PRIMARY_DATA_TYPE, PRIMARY_PATH);
        put(ASSOC_DATA_TYPE, ASSOC_PATH);
        put(INDIRECT_RELATED_DATA_TYPE, INDIRECT_RELATED_PATH);
        put(SECONDARY_INDIRECT_RELATED_DATA_TYPE, SECONDARY_INDIRECT_RELATED_PATH);
        put(TERTIARY_INDIRECT_RELATED_DATA_TYPE, TERTIARY_INDIRECT_RELATED_PATH);
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

    private final StructType directRelatedInputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(DIRECT_RELATED_FILTER_COLUMN, DataTypes.StringType, false)
    ));
    
    private final StructType directRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType directRelatedOutputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(DIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(DIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(DIRECT_RELATED_FILTER_COLUMN, DataTypes.StringType, false),
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

    private final StructType indirectRelatedInputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(INDIRECT_RELATED_FILTER_COLUMN, DataTypes.StringType, false)
    ));

    private final StructType indirectRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType indirectRelatedOutputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
            DataTypes.createStructField(INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
            DataTypes.createStructField(INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(INDIRECT_RELATED_FILTER_COLUMN, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
            DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType secondaryIndirectRelatedInputSchema = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));

    private final StructType secondaryIndirectRelatedInputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));

    private final StructType secondaryIndirectRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType secondaryIndirectRelatedOutputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(SECONDARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType tertiaryIndirectRelatedInputSchema = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));

    private final StructType tertiaryIndirectRelatedInputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false)
    ));

    private final StructType tertiaryIndirectRelatedOutputSchema = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final StructType tertiaryIndirectRelatedOutputSchemaWithFilterColumn = DataTypes.createStructType(Arrays.asList(
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_KEY_COLUMN, DataTypes.IntegerType, false),
        DataTypes.createStructField(TERTIARY_INDIRECT_RELATED_DATA_COLUMN, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.SOURCE_FACT_IDX, DataTypes.StringType, false),
        DataTypes.createStructField(ContextRetriever.JOIN_CONTEXT_VALUE_IDX, DataTypes.IntegerType, false)
    ));

    private final Relationship directJoin = newRelationship(
            DIRECT_RELATED_DATA_TYPE,
            "inner join " + DIRECT_RELATED_DATA_TYPE + " on " + DIRECT_RELATED_DATA_TYPE + "." + DIRECT_RELATED_KEY_COLUMN + " = " + PRIMARY_DATA_TYPE + "." + PRIMARY_KEY_COLUMN
    );

    private final Relationship directJoinWithAltKey = newRelationship(
            DIRECT_RELATED_DATA_TYPE,
            "inner join " + DIRECT_RELATED_DATA_TYPE + " on " + DIRECT_RELATED_DATA_TYPE + "." + DIRECT_RELATED_KEY_COLUMN + " = " + PRIMARY_DATA_TYPE + "." + PRIMARY_ALT_KEY_COLUMN
    );
    
    private final Relationship indirectJoin = newRelationship(
            INDIRECT_RELATED_DATA_TYPE,
            "inner join " + ASSOC_DATA_TYPE + " on " + PRIMARY_DATA_TYPE + "." + PRIMARY_KEY_COLUMN + " = " + ASSOC_DATA_TYPE + "." + ASSOC_LEFT_COLUMN + " inner join " + INDIRECT_RELATED_DATA_TYPE + " on " + INDIRECT_RELATED_DATA_TYPE + "." + INDIRECT_RELATED_KEY_COLUMN + " = " + ASSOC_DATA_TYPE + "." + ASSOC_RIGHT_COLUMN
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
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

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
		primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
		directRelatedDataset.createOrReplaceTempView(DIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

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
    public void retrieveContext_oneToManyJoinWithFilteringInPrimaryTable() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 0, "primary1"),
                RowFactory.create(2, 0, "primary2"),
                RowFactory.create(3, 1, "primary3"),
                RowFactory.create(4, 1, "primary4")
        );
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
        directRelatedDataset.createOrReplaceTempView(DIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);
        Relationship relationship = newRelationship(
                DIRECT_RELATED_DATA_TYPE,
                "inner join " + DIRECT_RELATED_DATA_TYPE + " on " + DIRECT_RELATED_DATA_TYPE + "." + DIRECT_RELATED_KEY_COLUMN + " = " + PRIMARY_DATA_TYPE + "." + PRIMARY_KEY_COLUMN + " and " + PRIMARY_ALT_KEY_COLUMN + " = 1"
        );

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(relationship)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 0, "primary1", PRIMARY_DATA_TYPE)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 0, "primary2", PRIMARY_DATA_TYPE)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 1, "primary3", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchema, 3, "direct31", DIRECT_RELATED_DATA_TYPE, 3)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 1, "primary4", PRIMARY_DATA_TYPE)
                ))
        );

        assertOutput(expected, actual);
    }

    @Test
    public void retrieveContext_oneToManyJoinWithFilteringRelatedTable() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4")
        );
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

        Dataset<Row> directRelatedDataset = newDataset(
                directRelatedInputSchemaWithFilterColumn,
                RowFactory.create(1, "direct11", "valid"),
                RowFactory.create(1, "direct12", "valid"),
                RowFactory.create(1, "direct13", "other"),
                RowFactory.create(2, "direct21", "valid"),
                RowFactory.create(2, "direct22", "other"),
                RowFactory.create(3, "direct31", "valid"),
                RowFactory.create(98, "unrelated1", "valid"),
                RowFactory.create(99, "unrelated2", "valid")
        );
        directRelatedDataset.createOrReplaceTempView(DIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

        Relationship relationship = newRelationship(
                DIRECT_RELATED_DATA_TYPE,
                "inner join " + DIRECT_RELATED_DATA_TYPE + " on " + DIRECT_RELATED_DATA_TYPE + "." + DIRECT_RELATED_KEY_COLUMN + " = " + PRIMARY_DATA_TYPE + "." + PRIMARY_KEY_COLUMN + " and " + DIRECT_RELATED_FILTER_COLUMN + " = 'valid'"
        );

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(relationship)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchemaWithFilterColumn, 1, "direct11", "valid", DIRECT_RELATED_DATA_TYPE, 1),
                        newRow(directRelatedOutputSchemaWithFilterColumn, 1, "direct12", "valid", DIRECT_RELATED_DATA_TYPE, 1)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchemaWithFilterColumn, 2, "direct21", "valid", DIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE),
                        newRow(directRelatedOutputSchemaWithFilterColumn, 3, "direct31", "valid", DIRECT_RELATED_DATA_TYPE, 3)
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
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
        directRelatedDataset.createOrReplaceTempView(DIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

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
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
        assocDataset.createOrReplaceTempView(ASSOC_DATA_TYPE);

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
        indirectRelatedDataset.createOrReplaceTempView(INDIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

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
    public void retrieveContext_manyToManyJoinWithFilter() {
        Dataset<Row> primaryDataset = newDataset(
                primaryInputSchema,
                RowFactory.create(1, 1, "primary1"),
                RowFactory.create(2, 2, "primary2"),
                RowFactory.create(3, 3, "primary3"),
                RowFactory.create(4, 4, "primary4")
        );
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
        assocDataset.createOrReplaceTempView(ASSOC_DATA_TYPE);

        Dataset<Row> indirectRelatedDataset = newDataset(
                indirectRelatedInputSchemaWithFilterColumn,
                RowFactory.create(21, "indirect21", "valid"),
                RowFactory.create(31, "indirect31", "other"),
                RowFactory.create(32, "indirect32", "other"),
                RowFactory.create(41, "indirect41", "other"),
                RowFactory.create(42, "indirect42", "other"),
                RowFactory.create(43, "indirect43", "valid"),
                RowFactory.create(198, "unrelated1", "valid"),
                RowFactory.create(199, "unrelated2", "valid")
        );
        indirectRelatedDataset.createOrReplaceTempView(INDIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

        Relationship relationship = newRelationship(
                INDIRECT_RELATED_DATA_TYPE,
                "inner join " + ASSOC_DATA_TYPE + " on " + PRIMARY_DATA_TYPE + "." + PRIMARY_KEY_COLUMN + " = " + ASSOC_DATA_TYPE + "." + ASSOC_LEFT_COLUMN + " inner join " + INDIRECT_RELATED_DATA_TYPE + " on " + INDIRECT_RELATED_DATA_TYPE + "." + INDIRECT_RELATED_KEY_COLUMN + " = " + ASSOC_DATA_TYPE + "." + ASSOC_RIGHT_COLUMN + " and " + INDIRECT_RELATED_FILTER_COLUMN + " = 'valid'" 
        );

        ContextDefinition contextDefinition = newContextDefinition(
                PRIMARY_NAME,
                PRIMARY_DATA_TYPE,
                PRIMARY_KEY_COLUMN,
                Collections.singletonList(relationship)
        );

        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();

        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
                new Tuple2<>(1, Arrays.asList(
                        newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE)
                )),
                new Tuple2<>(2, Arrays.asList(
                        newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchemaWithFilterColumn, 21, "indirect21", "valid", INDIRECT_RELATED_DATA_TYPE, 2)
                )),
                new Tuple2<>(3, Arrays.asList(
                        newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE)
                )),
                new Tuple2<>(4, Arrays.asList(
                        newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE),
                        newRow(indirectRelatedOutputSchemaWithFilterColumn, 43, "indirect43", "valid", INDIRECT_RELATED_DATA_TYPE, 4)
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
        primaryDataset.createOrReplaceTempView(PRIMARY_DATA_TYPE);

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
        directRelatedDataset.createOrReplaceTempView(DIRECT_RELATED_DATA_TYPE);

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
        assocDataset.createOrReplaceTempView(ASSOC_DATA_TYPE);

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
        indirectRelatedDataset.createOrReplaceTempView(INDIRECT_RELATED_DATA_TYPE);

        Map<String, Dataset<Row>> datasets = new HashMap<>();
        datasets.put(PRIMARY_PATH, primaryDataset);
        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);
        datasets.put(ASSOC_PATH, assocDataset);
        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);

        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);

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
                        newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 4),
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


//    @Test
//    public void retrieveContext_multipleIndirectJoins() {
//        Dataset<Row> primaryDataset = newDataset(
//            primaryInputSchema,
//            RowFactory.create(1, 1, "primary1"),
//            RowFactory.create(2, 2, "primary2"),
//            RowFactory.create(3, 3, "primary3"),
//            RowFactory.create(4, 4, "primary4"),
//            RowFactory.create(5, 5, "primary5")
//        );
//
//        Dataset<Row> directRelatedDataset = newDataset(
//            directRelatedInputSchema,
//            RowFactory.create(1, "direct11"),
//            RowFactory.create(1, "direct12"),
//            RowFactory.create(1, "direct13"),
//            RowFactory.create(2, "direct21"),
//            RowFactory.create(2, "direct22"),
//            RowFactory.create(3, "direct31"),
//            RowFactory.create(98, "unrelated1"),
//            RowFactory.create(99, "unrelated2")
//        );
//
//        Dataset<Row> assocDataset = newDataset(
//            assocInputSchema,
//            RowFactory.create(2, 21),
//            RowFactory.create(3, 31),
//            RowFactory.create(3, 32),
//            RowFactory.create(4, 32),
//            RowFactory.create(4, 41),
//            RowFactory.create(4, 42),
//            RowFactory.create(4, 43),
//            RowFactory.create(98, 198),
//            RowFactory.create(99, 199)
//        );
//
//        Dataset<Row> indirectRelatedDataset = newDataset(
//            indirectRelatedInputSchema,
//            RowFactory.create(21, "indirect21"),
//            RowFactory.create(31, "indirect31"),
//            RowFactory.create(32, "indirect32"),
//            RowFactory.create(41, "indirect41"),
//            RowFactory.create(42, "indirect42"),
//            RowFactory.create(43, "indirect43"),
//            RowFactory.create(198, "unrelated1"),
//            RowFactory.create(199, "unrelated2")
//        );
//
//        Dataset<Row> secondaryIndirectRelatedDataset = newDataset(
//            secondaryIndirectRelatedInputSchema,
//            RowFactory.create(21, "secondaryIndirect21"),
//            RowFactory.create(31, "secondaryIndirect31"),
//            RowFactory.create(32, "secondaryIndirect32"),
//            RowFactory.create(41, "secondaryIndirect41"),
//            RowFactory.create(43, "secondaryIndirect43"),
//            RowFactory.create(198, "unrelated1"),
//            RowFactory.create(199, "unrelated2")
//        );
//
//        Dataset<Row> tertiaryIndirectRelatedDataset = newDataset(
//            tertiaryIndirectRelatedInputSchema,
//            RowFactory.create(31, "tertiaryIndirect31"),
//            RowFactory.create(32, "tertiaryIndirect32"),
//            RowFactory.create(41, "tertiaryIndirect41")
//        );
//
//        Map<String, Dataset<Row>> datasets = new HashMap<>();
//        datasets.put(PRIMARY_PATH, primaryDataset);
//        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);
//        datasets.put(ASSOC_PATH, assocDataset);
//        datasets.put(INDIRECT_RELATED_PATH, indirectRelatedDataset);
//        datasets.put(SECONDARY_INDIRECT_RELATED_PATH, secondaryIndirectRelatedDataset);
//        datasets.put(TERTIARY_INDIRECT_RELATED_PATH, tertiaryIndirectRelatedDataset);
//
//        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
//        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);
//
//        MultiManyToMany multiIndirectJoin = new MultiManyToMany();
//        multiIndirectJoin.setPrimaryDataTypeColumn(indirectJoin.getPrimaryDataTypeColumn());
//        multiIndirectJoin.setRelatedDataType(indirectJoin.getRelatedDataType());
//        multiIndirectJoin.setRelatedKeyColumn(indirectJoin.getRelatedKeyColumn());
//        multiIndirectJoin.setAssociationDataType(indirectJoin.getAssociationDataType());
//        multiIndirectJoin.setAssociationOneKeyColumn(indirectJoin.getAssociationOneKeyColumn());
//        multiIndirectJoin.setAssociationManyKeyColumn(indirectJoin.getAssociationManyKeyColumn());
//        multiIndirectJoin.setWhereClause(indirectJoin.getWhereClause());
//
//        MultiManyToMany secondary = new MultiManyToMany();
//        secondary.setRelatedDataType(SECONDARY_INDIRECT_RELATED_DATA_TYPE);
//        secondary.setRelatedKeyColumn(SECONDARY_INDIRECT_RELATED_KEY_COLUMN);
//        secondary.setAssociationDataType(indirectJoin.getRelatedDataType());
//        secondary.setAssociationOneKeyColumn(indirectJoin.getRelatedKeyColumn());
//        secondary.setAssociationManyKeyColumn(indirectJoin.getRelatedKeyColumn());
//        multiIndirectJoin.setWith(secondary);
//
//        MultiManyToMany tertiary = new MultiManyToMany();
//        tertiary.setRelatedDataType(TERTIARY_INDIRECT_RELATED_DATA_TYPE);
//        tertiary.setRelatedKeyColumn(TERTIARY_INDIRECT_RELATED_KEY_COLUMN);
//        tertiary.setAssociationDataType(secondary.getRelatedDataType());
//        tertiary.setAssociationOneKeyColumn(secondary.getRelatedKeyColumn());
//        tertiary.setAssociationManyKeyColumn(secondary.getRelatedKeyColumn());
//        secondary.setWith(tertiary);
//        
//        ContextDefinition contextDefinition = newContextDefinition(
//            PRIMARY_NAME,
//            PRIMARY_DATA_TYPE,
//            PRIMARY_KEY_COLUMN,
//            Arrays.asList(directJoin, multiIndirectJoin)
//        );
//
//        List<Tuple2<Object, List<Row>>> actual = contextRetriever.retrieveContext(contextDefinition).collect();
//
//        List<Tuple2<Object, List<Row>>> expected = Arrays.asList(
//            new Tuple2<>(1, Arrays.asList(
//                newRow(primaryOutputSchema, 1, 1, "primary1", PRIMARY_DATA_TYPE),
//                newRow(directRelatedOutputSchema, 1, "direct11", DIRECT_RELATED_DATA_TYPE, 1),
//                newRow(directRelatedOutputSchema, 1, "direct12", DIRECT_RELATED_DATA_TYPE, 1),
//                newRow(directRelatedOutputSchema, 1, "direct13", DIRECT_RELATED_DATA_TYPE, 1)
//            )),
//            new Tuple2<>(2, Arrays.asList(
//                newRow(primaryOutputSchema, 2, 2, "primary2", PRIMARY_DATA_TYPE),
//                newRow(directRelatedOutputSchema, 2, "direct21", DIRECT_RELATED_DATA_TYPE, 2),
//                newRow(directRelatedOutputSchema, 2, "direct22", DIRECT_RELATED_DATA_TYPE, 2),
//                newRow(secondaryIndirectRelatedOutputSchema, 21, "secondaryIndirect21", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 2),
//                newRow(indirectRelatedOutputSchema, 21, "indirect21", INDIRECT_RELATED_DATA_TYPE, 2)
//            )),
//            new Tuple2<>(3, Arrays.asList(
//                newRow(primaryOutputSchema, 3, 3, "primary3", PRIMARY_DATA_TYPE),
//                newRow(directRelatedOutputSchema, 3, "direct31", DIRECT_RELATED_DATA_TYPE, 3),
//                newRow(secondaryIndirectRelatedOutputSchema, 32, "secondaryIndirect32", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 3),
//                newRow(secondaryIndirectRelatedOutputSchema, 31, "secondaryIndirect31", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 3),
//                newRow(tertiaryIndirectRelatedOutputSchema, 31, "tertiaryIndirect31", TERTIARY_INDIRECT_RELATED_DATA_TYPE, 3),
//                newRow(tertiaryIndirectRelatedOutputSchema, 32, "tertiaryIndirect32", TERTIARY_INDIRECT_RELATED_DATA_TYPE, 3),
//                newRow(indirectRelatedOutputSchema, 31, "indirect31", INDIRECT_RELATED_DATA_TYPE, 3),
//                newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 3)
//            )),
//            new Tuple2<>(4, Arrays.asList(
//                newRow(primaryOutputSchema, 4, 4, "primary4", PRIMARY_DATA_TYPE),
//                newRow(secondaryIndirectRelatedOutputSchema, 43, "secondaryIndirect43", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(secondaryIndirectRelatedOutputSchema, 41, "secondaryIndirect41", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(secondaryIndirectRelatedOutputSchema, 32, "secondaryIndirect32", SECONDARY_INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(tertiaryIndirectRelatedOutputSchema, 41, "tertiaryIndirect41", TERTIARY_INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(tertiaryIndirectRelatedOutputSchema, 32, "tertiaryIndirect32", TERTIARY_INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(indirectRelatedOutputSchema, 32, "indirect32", INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(indirectRelatedOutputSchema, 41, "indirect41", INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(indirectRelatedOutputSchema, 42, "indirect42", INDIRECT_RELATED_DATA_TYPE, 4),
//                newRow(indirectRelatedOutputSchema, 43, "indirect43", INDIRECT_RELATED_DATA_TYPE, 4)
//            )),
//            new Tuple2<>(5, Arrays.asList(
//                newRow(primaryOutputSchema, 5, 5, "primary5", PRIMARY_DATA_TYPE)
//            ))
//        );
//
//        assertOutput(expected, actual);
//    }
//
//    @Test
//    public void retrieveContext_unexpectedJoinType() {
//        Dataset<Row> primaryDataset = newDataset(
//                primaryInputSchema,
//                RowFactory.create(1, 1, "primary1")
//        );
//
//        Dataset<Row> directRelatedDataset = newDataset(
//                directRelatedInputSchema
//        );
//
//        Map<String, Dataset<Row>> datasets = new HashMap<>();
//        datasets.put(PRIMARY_PATH, primaryDataset);
//        datasets.put(DIRECT_RELATED_PATH, directRelatedDataset);
//
//        DatasetRetriever datasetRetriever = new TestDatasetRetriever(datasets);
//        ContextRetriever contextRetriever = new ContextRetriever(inputPaths, datasetRetriever, null);
//
//        Join customJoin = new Join() { };
//        customJoin.setRelatedDataType(DIRECT_RELATED_DATA_TYPE);
//        customJoin.setRelatedKeyColumn(DIRECT_RELATED_KEY_COLUMN);
//
//        ContextDefinition contextDefinition = newContextDefinition(
//                PRIMARY_NAME,
//                PRIMARY_DATA_TYPE,
//                PRIMARY_KEY_COLUMN,
//                Collections.singletonList(customJoin)
//        );
//
//        IllegalArgumentException e = Assert.assertThrows(
//                IllegalArgumentException.class,
//                () -> contextRetriever.retrieveContext(contextDefinition));
//        Assert.assertTrue(e.getMessage().contains("Unexpected Join Type"));
//    }
//
//    @Test
//    public void retrieveContext_noPathForDataType() {
//        Map<String, String> noPaths = Collections.emptyMap();
//        Map<String, Dataset<Row>> noDatasets = Collections.emptyMap();
//
//        DatasetRetriever datasetRetriever = new TestDatasetRetriever(noDatasets);
//        ContextRetriever contextRetriever = new ContextRetriever(noPaths, datasetRetriever, null);
//
//        ContextDefinition contextDefinition = newContextDefinition(
//                PRIMARY_NAME,
//                PRIMARY_DATA_TYPE,
//                PRIMARY_KEY_COLUMN,
//                Collections.emptyList()
//        );
//
//        IllegalArgumentException e = Assert.assertThrows(
//                IllegalArgumentException.class,
//                () -> contextRetriever.retrieveContext(contextDefinition));
//        Assert.assertTrue(e.getMessage().contains("No path mapping found for datatype"));
//    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private void assertOutput(List<Tuple2<Object, List<Row>>> expected, List<Tuple2<Object, List<Row>>> actual) {
        // Sort the outer and inner lists for the equals check.
        List<Tuple2<Object, List<Row>>> sortedExpected = new ArrayList<>(expected);
        sortedExpected.sort(Comparator.comparing(x -> (Comparable)x._1));
        for (Tuple2<Object, List<Row>> tuple : sortedExpected) {
            List<Row> rows = tuple._2();
            rows.sort(Comparator.comparing(x -> x.getAs(0)));
        }

        List<Tuple2<Object, List<Row>>> sortedActual = new ArrayList<>(actual);
        sortedActual.sort(Comparator.comparing(x -> (Comparable)x._1));
        for (Tuple2<Object, List<Row>> tuple : sortedActual) {
            List<Row> rows = tuple._2();
            rows.sort(Comparator.comparing(x -> x.getAs(0)));
        }

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
	
	private Relationship newRelationship(String name, String joinClause) {
		Relationship relationship = new Relationship();
		relationship.setJoinClause(joinClause);
		relationship.setName(name);
		return relationship;
	}

    private ContextDefinition newContextDefinition(String name, String dataType, String keyColumn, List<Relationship> relations) {
        ContextDefinition retVal = new ContextDefinition();
        retVal.setName(name);
        retVal.setPrimaryDataType(dataType);
        retVal.setPrimaryKeyColumn(keyColumn);
        retVal.setRelationships(relations);
        return retVal;
    }

}
