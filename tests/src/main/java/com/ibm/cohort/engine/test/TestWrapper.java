/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.ibm.cohort.cli.CohortCLI;
import com.ibm.cohort.cql.evaluation.ContextNames;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.Format;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

// KWAS TODO: This might not even be used by our toolchain tests...
// Can it be deleted outright alongside `TestDriver_MeasureEvaluator.py`?
public class TestWrapper {
    private CohortCLI cli;
    private CqlEvaluator engine;
    private ByteArrayOutputStream byteStream;
    private PrintStream ps;
    public TestWrapper()
    {   
        cli = new CohortCLI();
    }
    /**
     * @param d 'config/remote-hapi-fhir.json'
     * @param t 'config/remote-hapi-fhir.json'
     * @param f 'src/test/resources/cql/basic'
     * @param l 'Test'
     * @param c '1235008
     * @return Output stream
     * @throws Exception any exception
     */
    public String warm(String d, String t, String f, String l, String c) throws Exception
    {
        String[] args = new String[] {"-d", d, "-t", t, "-f", f, "-l", l, "-c", c};
        byteStream = new ByteArrayOutputStream();
        ps = new PrintStream(byteStream, true, StandardCharsets.UTF_8.name());
        engine = cli.runWithArgs(args, ps);
        String out =  byteStream.toString(StandardCharsets.UTF_8.name());
        byteStream.reset(); // Wipe the stream so the next run will have clean output.
        return out;
    }
    public String execute(String library, String contextId) throws Exception
    {
        // KWAS TODO: Either support versionless cql execution, or update the external tests to supply versions
        // The latter is probably easier.
        CqlLibraryDescriptor libraryDescriptor = new CqlLibraryDescriptor()
                .setLibraryId(library)
                .setVersion("???")
                .setFormat(Format.ELM);
        // KWAS TODO: Is the use of the Patient context here passible?
        // If not, then I may have to update the external tests.
        Pair<String, String> context = new ImmutablePair<>(ContextNames.PATIENT, contextId);
        CqlEvaluationResult result = engine.evaluate(libraryDescriptor, context);

        for (Map.Entry<String, Object> entry : result.getExpressionResults().entrySet()) {
            String expression = entry.getKey();
            Object resultValue = entry.getValue();
            ps.println(String.format(
                    "Expression: %s, Context: %s, Result: %s",
                    expression,
                    contextId,
                    (resultValue != null) ? resultValue.toString() : "null"
            ));
        }

        String out = byteStream.toString(StandardCharsets.UTF_8.name());
        byteStream.reset(); // Wipe the stream so the next run will have clean output.
        return out;
    }
}
