package com.ibm.cohort.cql.spark.data;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.cql.spark.BaseSparkTest;
import com.ibm.cohort.cql.util.EqualsStringMatcher;
import com.ibm.cohort.cql.util.RegexStringMatcher;

public class ColumnFilterFunctionTest extends BaseSparkTest {
	private static final long serialVersionUID = 1L;
	private SparkSession spark;

	@Before
	public void setUp() {
		this.spark = initializeSession(Java8API.ENABLED);
	}

	@Test
	public void testColumnFiltering() {
		String path = new File("src/test/resources/alltypes/testdata/test-A.parquet").toURI().toString();

		Dataset<Row> baseline = spark.read().format("parquet").load(path);
		assertEquals(12, baseline.schema().fields().length);

		String colName = "boolean_col";
		String regexColName = "code_col[0-9]*";

		ColumnFilterFunction datasetTransformer = new ColumnFilterFunction(new HashSet<>(Arrays.asList(new EqualsStringMatcher(colName), new RegexStringMatcher(regexColName))));
		Dataset<Row> filtered = datasetTransformer.apply(baseline);

		Set<String> expectedNames = new HashSet<>(Arrays.asList(colName, "code_col", "code_col_system", "string_col", "code_col2"));
		Set<String> actualNames = new HashSet<>(Arrays.asList(filtered.schema().fieldNames()));
		assertEquals(expectedNames, actualNames);
	}

	@Test
	public void testColumnFilteringNoColumnsRequired() {
		String path = new File("src/test/resources/alltypes/testdata/test-A.parquet").toURI().toString();
		Dataset<Row> baseline = spark.read().format("parquet").load(path);

		ColumnFilterFunction filteredRetriever = new ColumnFilterFunction(Collections.emptySet());
		Dataset<Row> filtered = filteredRetriever.apply(baseline);
		assertNull(filtered);
	}
}
