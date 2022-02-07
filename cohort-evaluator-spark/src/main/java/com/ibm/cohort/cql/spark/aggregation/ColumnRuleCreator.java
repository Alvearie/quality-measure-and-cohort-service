package com.ibm.cohort.cql.spark.aggregation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.spark.optimizer.DataTypeRequirementsProcessor;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.util.EqualsStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;

public class ColumnRuleCreator {
	private static final Logger LOG = LoggerFactory.getLogger(ColumnRuleCreator.class);

	private final List<CqlEvaluationRequest> requests;
	private final CqlToElmTranslator cqlTranslator;
	private final CqlLibraryProvider libraryProvider;
	
	public ColumnRuleCreator(List<CqlEvaluationRequest> requests, CqlToElmTranslator cqlTranslator, CqlLibraryProvider libraryProvider) {
		this.requests = requests;
		this.cqlTranslator = cqlTranslator;
		this.libraryProvider = libraryProvider;
	}

	/**
	 * Retrieve the merged set of data type and column filters for all CQL jobs that will
	 * be evaluated for a given aggregation context.
	 *
	 * @param context ContextDefinition whose CQL jobs will be interrogated for data requirements
	 * @return Map of data type to the fields in that datatype that are used by the CQL jobs
	 */
	public Map<String, Set<StringMatcher>> getDataRequirementsForContext(ContextDefinition context) {
		
		Map<CqlLibraryDescriptor,Set<String>> expressionsByLibrary = new HashMap<>();
		for( CqlEvaluationRequest request : requests ) {
			Set<String> expressions = expressionsByLibrary.computeIfAbsent( request.getDescriptor(), desc -> new HashSet<>() );
			request.getExpressions().stream().forEach( exp -> expressions.add(exp.getName()) );
		}

		DataTypeRequirementsProcessor requirementsProcessor = new DataTypeRequirementsProcessor(cqlTranslator);

		Map<String,Set<StringMatcher>> pathsByDataType = new HashMap<>();
		for( Map.Entry<CqlLibraryDescriptor, Set<String>> entry : expressionsByLibrary.entrySet() ) {
			LOG.debug("Extracting data requirements for {}", entry.getKey());

			DataTypeRequirementsProcessor.DataTypeRequirements requirements = requirementsProcessor.getDataRequirements(libraryProvider, entry.getKey(), entry.getValue());

			Map<String,Set<StringMatcher>> newPaths = requirements.allAsStringMatcher();

			newPaths.forEach( (key,value) -> {
				pathsByDataType.merge(key, value, (prev,current) -> { prev.addAll(current); return prev; } );
			});
		}

		Set<StringMatcher> contextFields = pathsByDataType.computeIfAbsent(context.getPrimaryDataType(), dt -> new HashSet<>() );
		contextFields.add(new EqualsStringMatcher(context.getPrimaryKeyColumn()));
		if( context.getRelationships() != null ) {
			for( Join join : context.getRelationships() ) {
				Set<StringMatcher> joinFields = pathsByDataType.get(join.getRelatedDataType());
				if( joinFields != null ) {
					joinFields.add(new EqualsStringMatcher(join.getRelatedKeyColumn()));
					joinFields.add(new EqualsStringMatcher(ContextRetriever.JOIN_CONTEXT_VALUE_IDX));

					// if the join key is not the primary key of the primary data table, then we need to add in the alternate key
					if( join.getPrimaryDataTypeColumn() != null ) {
						contextFields.add(new EqualsStringMatcher(join.getPrimaryDataTypeColumn()));
					}

					if( join instanceof ManyToMany) {
						ManyToMany manyToMany = (ManyToMany) join;
						Set<StringMatcher> associationFields = pathsByDataType.computeIfAbsent(manyToMany.getAssociationDataType(), dt -> new HashSet<>());
						associationFields.add(new EqualsStringMatcher(manyToMany.getAssociationOneKeyColumn()));
						associationFields.add(new EqualsStringMatcher(manyToMany.getAssociationManyKeyColumn()));
					}

					if (join instanceof MultiManyToMany) {
						ManyToMany with = ((MultiManyToMany) join).getWith();

						while (with != null) {
							Set<StringMatcher> relatedFields = pathsByDataType.computeIfAbsent(with.getRelatedDataType(), dt -> new HashSet<>());
							relatedFields.add(new EqualsStringMatcher(with.getRelatedKeyColumn()));
							relatedFields.add(new EqualsStringMatcher(ContextRetriever.JOIN_CONTEXT_VALUE_IDX));

							with = (with instanceof MultiManyToMany) ? ((MultiManyToMany) with).getWith() : null;
						}
					}
				}
			}
		}

		pathsByDataType.values().forEach((matcherSet -> {
			matcherSet.add(new EqualsStringMatcher(ContextRetriever.SOURCE_FACT_IDX));
		}));

		return pathsByDataType;
	}
}
