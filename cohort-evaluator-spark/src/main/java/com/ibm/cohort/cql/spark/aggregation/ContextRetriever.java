/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.spark.data.DatasetRetriever;
import com.ibm.cohort.cql.util.StringMatcher;

import scala.Tuple2;

/**
 * Handles the retrieval and organization of context data.
 *
 * Given a {@link DatasetRetriever} and mappings from datatype to path,
 * this class and process any number of contexts.
 */
public class ContextRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(ContextRetriever.class);
    
    /**
     * A column that tracks the source datatype for each data row.
     */
    public static final String SOURCE_FACT_IDX = "__SOURCE_FACT";

    /**
     * A column that tracks the context value for each data row.
     */
    public static final String JOIN_CONTEXT_VALUE_IDX = "__JOIN_CONTEXT_VALUE";

    private final Map<String, String> inputPaths;
    private final DatasetRetriever datasetRetriever;
    private final Map<String, Set<StringMatcher>> datatypeToColumnMatchers;

    /**
     * @param inputPaths A mapping from datatype to Hadoop compatible path
     * @param datasetRetriever A {@link DatasetRetriever} for low level data retrieval
     * @param datatypeToColumnMatchers Map of data type to required columns to use when building each context.
     *                                 When null is passed in, no column filtering is performed.   
     */
    public ContextRetriever(Map<String, String> inputPaths, DatasetRetriever datasetRetriever, Map<String, Set<StringMatcher>> datatypeToColumnMatchers) {
        this.inputPaths = inputPaths;
        this.datasetRetriever = datasetRetriever;
        this.datatypeToColumnMatchers = datatypeToColumnMatchers;
    }

    /**
     * Retrieves, joins, and organizes all data for a {@link ContextDefinition} into
     * a single {@link JavaPairRDD}.
     *
     * @param contextDefinition The {@link ContextDefinition} to process
     * @return A {@link JavaPairRDD} linking contextValue to a {@link List} of {@link Row}s
     */
    public JavaPairRDD<Object, List<Row>> retrieveContext(ContextDefinition contextDefinition) {
        // TRY temp view creation here.
        for (String dataType : inputPaths.keySet()) {
            Dataset<Row> ds = readDataset(dataType);
            ds.createOrReplaceTempView(dataType);
        }
        
        List<JavaPairRDD<Object, Row>> rddList = gatherRDDs(contextDefinition);

        JavaPairRDD<Object, Row> allData = unionPairRDDs(rddList, contextDefinition.getName());

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

    /**
     * Creates a {@link JavaPairRDD} for a context's primary datatype and all
     * underlying joins.
     *
     * @param contextDefinition The {@link ContextDefinition} to process
     * @return A {@link List} of {@link JavaPairRDD}s mapping context value to {@link Row}.
     *         It is expected for a context value to have multiple mappings within an RDD.
     */
    private List<JavaPairRDD<Object, Row>> gatherRDDs(ContextDefinition contextDefinition) {
        List<JavaPairRDD<Object, Row>> retVal = new ArrayList<>();

        String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();
        String primaryDataType = contextDefinition.getPrimaryDataType();
        
        // TODO: Figure out how to register temp views in one central place.
        // TODO: Figure out column filtering so we only select columns needed in CQL.
        // TODO: Check which tests fail due to ContextDefinition changes. Fix those tests.
        
        // I think reading the primary dataset starts off as select * from primaryDataType
        // along with any extra columns that are needed
        Dataset<Row> primaryDataset = readDataset(primaryDataType);
        retVal.add(toPairRDD(primaryDataset, primaryKeyColumn));

        // This entire block likely goes away and becomes a single sql statement.
        // The join/where clause logic should be pretty straightforward.
        // Column filtering will take some thought to get right. Have to look at the column matchers
        // and figure out if they are ultimately just column names or what.
        List<Relationship> relationships = contextDefinition.getRelationships() == null
                ? Collections.emptyList()
                : contextDefinition.getRelationships();

        SparkSession sparkSession = SparkSession.getActiveSession().get();
        for (Relationship relationship : relationships) {
            // Delete this block. Just want to clear pmd violations while working
            if (datatypeToColumnMatchers == null || datatypeToColumnMatchers.isEmpty()) {
                LOG.info("No column matcher information provided.");
            }
            
            // select primary.primekey as SPECIAL, thistype.* from primary 
            StringBuilder sb = new StringBuilder("select ");
            sb = sb.append(relationship.getName()).append(".*, ")
                    .append(primaryDataType).append('.').append(primaryKeyColumn).append(" as ").append(JOIN_CONTEXT_VALUE_IDX)
                    .append(" from ").append(primaryDataType).append(' ')
                    .append(relationship.getJoinClause());

            Dataset<Row> result = sparkSession.sql(sb.toString());
            retVal.add(toPairRDD(result, JOIN_CONTEXT_VALUE_IDX));
        }

        return retVal;
    }

    /**
     * Given a set of rows that are indexed by context value, reorganize the data so
     * that all rows related to the same context are grouped into a single pair.
     *
     * @param allData Rows mapped from context value to a single data row.
     * @return A single {@link JavaPairRDD} mapping context value to
     *         the list of all data for that context.
     */
    private JavaPairRDD<Object, List<Row>> groupPairRDDs(JavaPairRDD<Object, Row> allData) {
        // Regroup data by context ID so that all input data for the same
        // context is represented as a single key mapped to a list of rows
        return allData.combineByKey(
                (val) -> {
                    List<Row> retVal = new ArrayList<>();
                    retVal.add(val);
                    return retVal;
                },
                (list, val) -> {
                    list.add(val);
                    return list;
                },
                (list1, list2) -> {
                    int numRows = list1.size() + list2.size();
                    List<Row> retVal = new ArrayList<>(numRows);
                    retVal.addAll(list1);
                    retVal.addAll(list2);
                    return retVal;
                }
        );
    }

    /**
     * Converts a {@link Dataset} to {@link JavaPairRDD} that maps the value from
     * the provided column to the respective {@link Row}.
     *
     * @param dataset The {@link Dataset} to convert.
     * @param contextColumn The column to use as the source for the context value.
     * @return A {@link JavaPairRDD} from context value to {@link Row}.
     */
    private JavaPairRDD<Object, Row> toPairRDD(Dataset<Row> dataset, String contextColumn) {
        return dataset.javaRDD().mapToPair(row -> {
            Object joinValue = row.getAs(contextColumn);
            return new Tuple2<>(joinValue, row);
        });
    }

    /**
     * Unions multiple {@link JavaPairRDD}s into one larger {@link JavaPairRDD}.
     *
     * @param rddList The list of RDDs to union.
     * @param context The name of the context being unioned.
     * @param <K> The key of the RDD.
     * @param <V> The value of the RDD.
     * @return A fully unioned RDD.
     */
    private <K, V> JavaPairRDD<K, V> unionPairRDDs(List<JavaPairRDD<K, V>> rddList, String context) {
        if (rddList.isEmpty()) {
            throw new IllegalStateException(String.format("Provided context %s returned zero readable RDDs", context));
        }

        Iterator<JavaPairRDD<K, V>> rdds = rddList.iterator();
        JavaPairRDD<K, V> union = rdds.next();

        while (rdds.hasNext()) {
            union = union.union(rdds.next());
        }

        return union;
    }

    /**
     * Reads the data for the specified datatype into a {@link Dataset}.
     * Adds the special {@value SOURCE_FACT_IDX} column to track what
     * datatype a particular {@link Row} came from.
     * @param dataType The datatype to read.
     * @return A {@link Dataset} containing the data for the specified datatype.
     */
    private Dataset<Row> readDataset(String dataType) {
        String path = inputPaths.get(dataType);
        if (path == null) {
            throw new IllegalArgumentException(String.format("No path mapping found for datatype %s", dataType));
        }

        Dataset<Row> dataset = datasetRetriever.readDataset(path);
        if (dataset == null) {
            throw new IllegalArgumentException(String.format("Could not read data for %s", dataType));
        }

        return dataset.withColumn(SOURCE_FACT_IDX, functions.lit(dataType));
    }

}
