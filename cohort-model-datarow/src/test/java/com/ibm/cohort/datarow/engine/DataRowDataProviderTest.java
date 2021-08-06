package com.ibm.cohort.datarow.engine;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.DateTime;

import com.ibm.cohort.cql.data.CompositeCqlDataProvider;
import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.fs.DirectoryBasedCqlLibraryProvider;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;
import com.ibm.cohort.cql.terminology.UnsupportedTerminologyProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.TranslatingCqlLibraryProvider;
import com.ibm.cohort.datarow.model.SimpleDataRow;

public class DataRowDataProviderTest {
    @Test
    public void testEvaluateSuccess() throws Exception {
        CqlLibraryProvider backingProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/cql"));

        CqlToElmTranslator translator = new CqlToElmTranslator();
        try (Reader r = new FileReader(new File("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml"))) {
            translator.registerModelInfo(r);
        }
        CqlLibraryProvider libraryProvider = new TranslatingCqlLibraryProvider(backingProvider, translator);

        CqlLibraryDescriptor topLevelLibrary = new CqlLibraryDescriptor().setLibraryId("SampleLibrary")
                .setVersion("1.0.0").setFormat(Format.CQL);

        CqlTerminologyProvider terminologyProvider = new UnsupportedTerminologyProvider();

        Map<String, Object> row = new HashMap<>();
        row.put("id", "123");
        row.put("gender", "female");
        row.put("birthDate", new DateTime(OffsetDateTime.now()));

        Map<String, Iterable<Object>> data = new HashMap<>();
        data.put("Patient", Arrays.asList(new SimpleDataRow(row)));

        DataRowRetrieveProvider retrieveProvider = new DataRowRetrieveProvider(data, terminologyProvider);

        CqlDataProvider dataProvider = new CompositeCqlDataProvider(new DataRowModelResolver(SimpleDataRow.class), retrieveProvider);

        CqlEvaluator evaluator = new CqlEvaluator()
                .setLibraryProvider(libraryProvider)
                .setTerminologyProvider(terminologyProvider)
                .setDataProvider(dataProvider);

        CqlEvaluationResult result = evaluator.evaluate(topLevelLibrary);
        assertEquals(2, result.getExpressionResults().size());
        
        Object perDefineResult = result.getExpressionResults().get("IsFemale");
        assertEquals( Boolean.TRUE, perDefineResult );
    }
}
