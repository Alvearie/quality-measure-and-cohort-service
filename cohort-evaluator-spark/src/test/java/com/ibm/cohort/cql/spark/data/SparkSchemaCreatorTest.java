/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;

import scala.Tuple2;

@SuppressWarnings("serial")
public class SparkSchemaCreatorTest {
	// can hand build request / context definitions if needed
	
	private CqlLibraryProvider cqlLibraryProvider;
	private DefaultSparkOutputColumnEncoder outputColumnNameFactory;
	private CqlToElmTranslator cqlTranslator;
	
	@Before
	public void setup() {
		cqlTranslator = new CqlToElmTranslator();
		cqlTranslator.registerModelInfo(new File("src/test/resources/output-validation/modelinfo/simple-all-types-model-info.xml"));

		CqlLibraryProvider backingProvider = new ClasspathCqlLibraryProvider("output-validation.cql");
		cqlLibraryProvider = new TranslatingCqlLibraryProvider(
				backingProvider,
				cqlTranslator
		);
		
		outputColumnNameFactory = new DefaultSparkOutputColumnEncoder(".");
	}
	
	@Test
	public void singleContextSupportedDefineTypes() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Collections.singletonList(makeContextDefinition("A", "Type1", "id"))
		);
				
				
		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Arrays.asList("define_integer", "define_boolean", "define_string", "define_decimal")),
								"A"
						),
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context2Id").setVersion("1.0.0"),
								new HashSet<>(Arrays.asList("define_date", "define_datetime")),
								"A"
						)
				)
		);
		
		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		StructType actualSchema = schemaCreator.calculateSchemasForContexts(Arrays.asList("A")).get("A");

		StructType expectedSchema = new StructType()
				.add("id", DataTypes.IntegerType, false)
				.add("parameters", DataTypes.StringType, false)
				.add("Context1Id.define_integer", DataTypes.IntegerType, true)
				.add("Context1Id.define_boolean", DataTypes.BooleanType, true)
				.add("Context1Id.define_string", DataTypes.StringType, true)
				.add("Context1Id.define_decimal", DataTypes.createDecimalType(28, 8), true)
				.add("Context2Id.define_date", DataTypes.DateType, true)
				.add("Context2Id.define_datetime", DataTypes.TimestampType, true);
		
		validateSchemas(expectedSchema, actualSchema, "id");
	}

	@Test(expected = IllegalArgumentException.class)
    public void testLibraryNotFound() throws Exception {
        ContextDefinitions contextDefinitions = makeContextDefinitions(
                Collections.singletonList(makeContextDefinition("Context1Id", "Type1", "id"))
        );


        CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
                Arrays.asList(
                        makeEvaluationRequest(
                                new CqlLibraryDescriptor().setLibraryId("NotExists").setVersion("1.0.0"),
                                new HashSet<>(Collections.singletonList("bad-define")),
                                "Context1Id"
                        )
                )
        );

        SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
        schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
    }
	
	@Test(expected = IllegalArgumentException.class)
	public void testDefineNotFoundInLibrary() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Collections.singletonList(makeContextDefinition("Context1Id", "Type1", "id"))
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("bad-define")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}

	@Test
	public void testMultipleContexts() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "id"),
						makeContextDefinition("Context2Id", "Type2", "id")
				)
		);
		
		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_integer")),
								"Context1Id"
						),
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context2Id").setVersion("1.0.0"),
								new HashSet<>(Arrays.asList("define_date", "define_datetime")),
								"Context2Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		Map<String, StructType> actualSchemas = schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id", "Context2Id"));


		Map<String, Tuple2<String, StructType>> expectedSchemas = new HashMap<String, Tuple2<String, StructType>>() {{
			put("Context1Id", new Tuple2<>("id", new StructType()
					.add("id", DataTypes.IntegerType, false)
					.add("parameters", DataTypes.StringType, false)
					.add("Context1Id.define_integer", DataTypes.IntegerType, true)));
			put("Context2Id", new Tuple2<>("id", new StructType()
					.add("id", DataTypes.IntegerType, false)
					.add("parameters", DataTypes.StringType, false)
					.add("Context2Id.define_date", DataTypes.DateType, true)
					.add("Context2Id.define_datetime", DataTypes.TimestampType, true)));
		}};

		for (Map.Entry<String, Tuple2<String, StructType>> entry : expectedSchemas.entrySet()) {
			String contextName = entry.getKey();

			validateSchemas(
					expectedSchemas.get(contextName)._2(),
					actualSchemas.get(contextName),
					expectedSchemas.get(contextName)._1()
			);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultipleContextDefinitionsForContext() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "id"),
						makeContextDefinition("Context1Id", "Type1", "id")
				)
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_boolean")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidKeyColumn() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "other")
				)
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_boolean")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateTypeInformation() throws Exception {
		cqlTranslator.registerModelInfo(new File("src/test/resources/schema-validation/duplicate-type-model-info.xml"));
		
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "integer")
				)
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_integer")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicateElementInformation() throws Exception {
		cqlTranslator.registerModelInfo(new File("src/test/resources/schema-validation/duplicate-element-model-info.xml"));

		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "integer")
				)
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_integer")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnsupportedKeyColumnType() throws Exception {
		ContextDefinitions contextDefinitions = makeContextDefinitions(
				Arrays.asList(
						makeContextDefinition("Context1Id", "Type1", "code")
				)
		);


		CqlEvaluationRequests cqlEvaluationRequests = makeEvaluationRequests(
				Arrays.asList(
						makeEvaluationRequest(
								new CqlLibraryDescriptor().setLibraryId("Context1Id").setVersion("1.0.0"),
								new HashSet<>(Collections.singletonList("define_integer")),
								"Context1Id"
						)
				)
		);

		SparkSchemaCreator schemaCreator = new SparkSchemaCreator(cqlLibraryProvider, cqlEvaluationRequests, contextDefinitions, outputColumnNameFactory, cqlTranslator);
		schemaCreator.calculateSchemasForContexts(Arrays.asList("Context1Id"));
	}
	
	private void validateSchemas(StructType expectedSchema, StructType actualSchema, String keyColumnName) {
		StructField actualKeyColumn = actualSchema.fields()[0];
		StructField[] actualResultsColumns = Arrays.copyOfRange(actualSchema.fields(), 1, actualSchema.fields().length);

		StructField expectedKeyColumn = expectedSchema.fields()[0];
		StructField[] expectedResultsColumns = Arrays.copyOfRange(expectedSchema.fields(), 1, expectedSchema.fields().length);
		
		assertEquals(actualSchema.fieldNames().length, expectedSchema.fieldNames().length);
		assertEquals(expectedKeyColumn, actualKeyColumn);
		assertEquals(keyColumnName, actualKeyColumn.name());

		assertTrue(Arrays.asList(actualResultsColumns).containsAll(Arrays.asList(expectedResultsColumns)));
		assertTrue(Arrays.asList(expectedResultsColumns).containsAll(Arrays.asList(actualResultsColumns)));
	}
	
	private ContextDefinition makeContextDefinition(
			String name,
			String primaryDataType,
			String primaryKeyColumn
	) {
		ContextDefinition contextDefinition = new ContextDefinition();
		contextDefinition.setName(name);
		contextDefinition.setPrimaryDataType(primaryDataType);
		contextDefinition.setPrimaryKeyColumn(primaryKeyColumn);
		
		return contextDefinition;
	}
	
	private ContextDefinitions makeContextDefinitions(List<ContextDefinition> definitionList) {
		ContextDefinitions definitions = new ContextDefinitions();
		definitions.setContextDefinitions(definitionList);
		
		return definitions;
	}

	private CqlEvaluationRequest makeEvaluationRequest(
			CqlLibraryDescriptor descriptor,
			Set<String> expressions,
			String contextKey
	) {
		CqlEvaluationRequest evaluationRequest = new CqlEvaluationRequest();
		evaluationRequest.setDescriptor(descriptor);
		evaluationRequest.setExpressionsByNames(expressions);
		evaluationRequest.setContextKey(contextKey);
		return evaluationRequest;
	}

	private CqlEvaluationRequests makeEvaluationRequests(List<CqlEvaluationRequest> evaluationRequestList) {
		CqlEvaluationRequests requests = new CqlEvaluationRequests();
		requests.setEvaluations(evaluationRequestList);
		
		return requests;
	}
}
