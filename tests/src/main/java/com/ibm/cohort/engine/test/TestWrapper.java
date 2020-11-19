/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IAnyResource;

import com.ibm.cohort.cli.CohortCLI;
import com.ibm.cohort.engine.CqlEngineWrapper;
import com.ibm.cohort.engine.EvaluationResultCallback;

public class TestWrapper {
	private CohortCLI cli;
	private CqlEngineWrapper engine;
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
	 * @return
	 * @throws Exception
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
    	List<String> contextIds = new LinkedList<String>();
    	contextIds.add(contextId);
    	engine.evaluate(library, null, null, null, contextIds, new EvaluationResultCallback() {

			@Override
			public void onContextBegin(String contextId) {
				ps.println("Context: " + contextId);
			}

			@Override
			public void onEvaluationComplete(String contextId, String expression, Object result) {
			
				String value;
				if( result != null ) {
					if( result instanceof IAnyResource ) {
						IAnyResource resource = (IAnyResource) result;
						value = resource.getId();
					} else if( result instanceof Collection ) {
						Collection<?> collection = (Collection<?>) result;
						value = "Collection: " + collection.size();
					} else {
						value = result.toString();
					}
				} else {
					value = "null";
				}
				
				ps.println(String.format("Expression: \"%s\", Result: %s", expression, value));
			}

			@Override
			public void onContextComplete(String contextId) {
				ps.println("---");
			}
		});
    	String out =  byteStream.toString(StandardCharsets.UTF_8.name());
        byteStream.reset(); // Wipe the stream so the next run will have clean output.
        return out;
    }
}
