/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cqframework.cql.cql2elm.ModelInfoLoader;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinition;
import com.ibm.cohort.cql.spark.aggregation.ContextDefinitions;

import scala.Tuple2;


public class SparkSchemaCreator {
	private CqlLibraryProvider libraryProvider;
	private CqlEvaluationRequests requests;
	private ContextDefinitions contextDefinitions;

	public SparkSchemaCreator(CqlLibraryProvider libraryProvider, CqlEvaluationRequests requests, ContextDefinitions contextDefinitions) {
		this.libraryProvider = libraryProvider;
		this.requests = requests;
		this.contextDefinitions = contextDefinitions;
	}

	public Map<String, StructType> calculateSchemasForContexts(List<String> contextNames) throws Exception {
		HashMap<String, StructType> contextResultSchemas = new HashMap<>();

		for (String contextName : contextNames) {
			StructType schema = calculateSchemaForContext(contextName);
			if (schema != null) {
				contextResultSchemas.put(contextName, schema);
			}
		}
		
		return contextResultSchemas;
	}

	public StructType calculateSchemaForContext(String contextName) throws Exception {
		List<CqlEvaluationRequest> filteredRequests = requests.getEvaluationsForContext(contextName);

		StructType resultsSchema = new StructType();

		Set<Tuple2<String, String>> usingInfo = new HashSet<>();

		for (CqlEvaluationRequest filteredRequest : filteredRequests) {
			CqlLibraryDescriptor descriptor = filteredRequest.getDescriptor();
			String libraryId = descriptor.getLibraryId();

			for (String expression : filteredRequest.getExpressions()) {
				Library library = CqlLibraryReader.read(
						libraryProvider.getLibrary(new CqlLibraryDescriptor().setLibraryId(libraryId).setVersion(descriptor.getVersion())).getContentAsStream()
				);

				// Track the set of non-system using statements across libraries.
				// Information is used later to access ModelInfos when searching
				// for context key column type information.
				usingInfo.addAll(library.getUsings().getDef().stream()
										 .filter(x -> !x.getLocalIdentifier().equals("System"))
										 .map(x -> new Tuple2<>(x.getLocalIdentifier(), x.getVersion()))
										 .collect(Collectors.toList()));

				List<ExpressionDef> expressionDefs = library.getStatements().getDef().stream()
						.filter(x -> x.getName().equals(expression))
						.collect(Collectors.toList());

				if (expressionDefs.isEmpty()) {
					throw new IllegalArgumentException("Expression " + expression + " is configured in the CQL jobs file, but not found in "
															   + descriptor.getLibraryId() + "." + descriptor.getVersion());
				}

				QName resultTypeName = expressionDefs.get(0).getExpression().getResultTypeName();
				resultsSchema = resultsSchema.add(libraryId + "." + expression, QNameToDataTypeConverter.getFieldType(resultTypeName), true);
			}
		}

		if (resultsSchema.fields().length > 0) {
			Tuple2<String, DataType> keyInformation = getDataTypeForContextKey(contextName, usingInfo);
			StructType fullSchema = new StructType()
					.add(keyInformation._1(), keyInformation._2(), false);

			for (StructField field : resultsSchema.fields()) {
				fullSchema = fullSchema.add(field);
			}
			resultsSchema = fullSchema;
		}
		
		return resultsSchema;
	}

	private Tuple2<String, DataType> getDataTypeForContextKey(String contextName, Set<Tuple2<String, String>> usingInfos) {
		ContextDefinition contextDefinition = contextDefinitions.getContextDefinitionByName(contextName);
		
		String primaryDataType = contextDefinition.getPrimaryDataType();
		String primaryKeyColumn = contextDefinition.getPrimaryKeyColumn();

		DataType keyType = null;

		// Check the model info for each non-system using statement from the libraries run for this context.
		// Try to find the key column's type information from a single model info.
		for (Tuple2<String, String> usingInfo : usingInfos) {
			VersionedIdentifier modelInfoIdentifier = new VersionedIdentifier().withId(usingInfo._1()).withVersion(usingInfo._2());
			ModelInfo modelInfo = ModelInfoLoader.getModelInfoProvider(modelInfoIdentifier).load();

			// Look for a ClassInfo element matching primaryDataType for the context
			List<ClassInfo> classInfos = modelInfo.getTypeInfo().stream()
					.map(x -> (ClassInfo) x)
					.filter(x -> x.getName().equals(primaryDataType))
					.collect(Collectors.toList());

			if (!classInfos.isEmpty()) {
				if (classInfos.size() == 1) {
					List<ClassInfoElement> elements = classInfos.get(0).getElement().stream()
							.filter(x -> x.getName().equals(primaryKeyColumn))
							.collect(Collectors.toList());

					// A future ModelInfo file may contain the information
					if (elements.isEmpty()) {
						continue;
					}
					else if (elements.size() == 1) {
						String elementType = elements.get(0).getElementType();

						// store it
						if (keyType == null) {
							keyType = getSparkTypeForSystemValue(elementType);
						} else {
							throw new IllegalArgumentException(
									"Multiple definitions found for " + primaryDataType + "." + primaryKeyColumn
											+ " in the provided ModelInfo files. Cannot infer key type for context: " + contextName);
						}
					}
					else if (elements.size() > 1) {
						throw new IllegalArgumentException("ModelInfo " + modelInfoIdentifier + " contains multiple element definitions for " + primaryKeyColumn + " for type " + primaryDataType);
					}
				}
				else {
					throw new IllegalArgumentException("ModelInfo " + modelInfoIdentifier + " contains multiple definitions for type " + primaryDataType);
				}
			}

		}
		
		if (keyType == null) {
			throw new IllegalArgumentException(
					"Could not locate type information for " + primaryDataType + "." + primaryKeyColumn
							+ " in the provided ModelInfo files. Cannot infer key type for context: " + contextName);			
		}
		return new Tuple2<>(contextDefinition.getPrimaryKeyColumn(), keyType);
	}
	
	private DataType getSparkTypeForSystemValue(String elementType) {
		DataType dataType = null;

		// Assuming system types are of format "System.TYPE"
		if (elementType != null){
			String[] split = elementType.split("\\.");
			if (split.length == 2) {
				dataType = QNameToDataTypeConverter.getFieldType(QNameToDataTypeConverter.createQNameForElmNamespace(split[1]));
			}
		}
		
		return dataType;
	}
}
