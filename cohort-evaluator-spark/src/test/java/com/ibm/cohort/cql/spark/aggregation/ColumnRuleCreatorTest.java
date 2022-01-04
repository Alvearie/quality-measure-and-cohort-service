package com.ibm.cohort.cql.spark.aggregation;


import static junit.framework.TestCase.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequests;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.cql.util.EqualsStringMatcher;
import com.ibm.cohort.cql.util.StringMatcher;

public class ColumnRuleCreatorTest {

    @Test
    public void testGetFiltersForContext() throws Exception {
        CqlToElmTranslator cqlTranslator = new CqlToElmTranslator();
        cqlTranslator.registerModelInfo(new File("src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml"));

		ObjectMapper mapper = new ObjectMapper();
		CqlEvaluationRequests requests = mapper.readValue(new File("src/test/resources/alltypes/metadata/parent-child-jobs.json"), CqlEvaluationRequests.class);

		TranslatingCqlLibraryProvider cqlLibraryProvider = new TranslatingCqlLibraryProvider(new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/alltypes/cql")), cqlTranslator);

		ColumnRuleCreator columnRuleCreator = new ColumnRuleCreator(
				requests.getEvaluations(),
				cqlTranslator,
				cqlLibraryProvider
		);

		ContextDefinitions definitions = mapper.readValue(new File("src/test/resources/alltypes/metadata/context-definitions.json"), ContextDefinitions.class);
		ContextDefinition context = definitions.getContextDefinitionByName("Patient");
        
        Map<String, Set<StringMatcher>> actual = columnRuleCreator.getDataRequirementsForContext(context);
        
        Map<String,Set<StringMatcher>> expected = new HashMap<>();
        expected.put("A", new HashSet<>(Arrays.asList(new EqualsStringMatcher(ContextRetriever.SOURCE_FACT_IDX), new EqualsStringMatcher("pat_id"),
													  new EqualsStringMatcher("code_col"), new EqualsStringMatcher("boolean_col"))));
        
        assertEquals( expected, actual );
    }

    @Test
    public void testGetFiltersForContextOnlyJoinColumns() throws Exception {
        CqlToElmTranslator cqlTranslator = new CqlToElmTranslator();
        cqlTranslator.registerModelInfo(new File("src/test/resources/alltypes/modelinfo/alltypes-modelinfo-1.0.0.xml"));

		ObjectMapper mapper = new ObjectMapper();
		CqlEvaluationRequests requests = mapper.readValue(new File("src/test/resources/alltypes/metadata/join-only.json"), CqlEvaluationRequests.class);

		TranslatingCqlLibraryProvider cqlLibraryProvider = new TranslatingCqlLibraryProvider(new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/alltypes/cql")), cqlTranslator);

		ColumnRuleCreator columnRuleCreator = new ColumnRuleCreator(
				requests.getEvaluations(),
				cqlTranslator,
				cqlLibraryProvider
		);

		ContextDefinitions definitions = mapper.readValue(new File("src/test/resources/alltypes/metadata/context-definitions-related-column.json"), ContextDefinitions.class);
		ContextDefinition context = definitions.getContextDefinitionByName("Patient");

		Map<String, Set<StringMatcher>> actual = columnRuleCreator.getDataRequirementsForContext(context);

        Map<String,Set<StringMatcher>> expected = new HashMap<>();
        expected.put("A", new HashSet<>(Arrays.asList(new EqualsStringMatcher(ContextRetriever.SOURCE_FACT_IDX), new EqualsStringMatcher("id_col"), new EqualsStringMatcher("pat_id"))));
        expected.put("B", new HashSet<>(Arrays.asList(new EqualsStringMatcher(ContextRetriever.SOURCE_FACT_IDX), new EqualsStringMatcher("string"), new EqualsStringMatcher(ContextRetriever.JOIN_CONTEXT_VALUE_IDX))));
        expected.put("C", new HashSet<>(Arrays.asList(new EqualsStringMatcher(ContextRetriever.SOURCE_FACT_IDX), new EqualsStringMatcher("pat_id"), new EqualsStringMatcher(ContextRetriever.JOIN_CONTEXT_VALUE_IDX))));

        assertEquals( expected, actual );
    }
}