/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.junit.Test;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

/**
 * Validate that engine parameters of all types can be set both with value or
 * using CQL-defined default value. This was raised as an important topic
 * given our need to support parameterized-overrides and because early
 * testing of the CQL Engine demonstrated some buggy behavior (fixed in 1.5.1).
 */
public class CQLParameterTests {
	@Test
	public void testIntegerWithDefault() throws Exception {
		runTest("Integer", 10, "10");
	}

	@Test
	public void testIntegerNoDefault() throws Exception {
		runTest("Integer", 100);
	}

	@Test
	public void testDecimalWithDefault() throws Exception {
		runTest("Decimal", new BigDecimal(11), "11");
	}

	@Test
	public void testDecimalNoDefault() throws Exception {
		runTest("Decimal", new BigDecimal(100.1));
	}

	@Test
	public void testBooleanWithDefault() throws Exception {
		runTest("Boolean", Boolean.TRUE, "true");
	}

	@Test
	public void testBooleanNoDefault() throws Exception {
		runTest("Boolean", Boolean.FALSE);
	}

	@Test
	public void testStringWithDefault() throws Exception {
		runTest("String", "Corey", "'Corey'");
	}

	@Test
	public void testStringNoDefault() throws Exception {
		runTest("String", "Sanders");
	}

	@Test
	public void testDateWithDefault() throws Exception {
		runTest("Date", new Date(1978, 8, 01), "@1978-08-01");
	}

	@Test
	public void testDateNoDefault() throws Exception {
		runTest("Date", new Date(2020, 10, 12));
	}
	
	@Test
	public void testDateTimeWithDefault() throws Exception {
		runTest("DateTime", new DateTime("1978-08-01T00:00:00.000", OffsetDateTime.now().getOffset()), "@1978-08-01");
	}

	@Test
	public void testDateTimeNoDefault() throws Exception {
		runTest("DateTime", new DateTime("2020-10-12", OffsetDateTime.now().getOffset()));
	}
	
	@Test
	public void testTimeWithDefault() throws Exception {
		runTest("Time", new Time(12, 13, 14), "@T12:13:14");
	}

	@Test
	public void testTimeNoDefault() throws Exception {
		runTest("Time", new Time(1, 2, 3));
	}	

	@Test
	public void testQuantityWithDefault() throws Exception {
		runTest("Quantity", new Quantity().withValue(new BigDecimal(10)).withUnit("mg/mL"), "10 'mg/mL'");
	}

	@Test
	public void testQuantityNoDefault() throws Exception {
		runTest("Quantity", new Quantity().withValue(new BigDecimal(100)).withUnit("mmol"));
	}

	@Test
	public void testCodeWithDefault() throws Exception {
		runTest("Code", new Code().withCode("ABC").withSystem("urn:mysystem").withDisplay("Alphabet"),
				"Code 'ABC' from \"DUMMY\" display 'Alphabet'");
	}

	@Test
	public void testCodeNoDefault() throws Exception {
		runTest("Code", new Code().withCode("DEF").withSystem("DUMMY").withDisplay("IBM"));
	}

	@Test
	public void testConceptWithDefault() throws Exception {
		runTest("Concept",
				new Concept().withCode(new Code().withCode("ABC").withSystem("urn:mysystem")).withDisplay("MyConcept"),
				"Concept { Code 'ABC' from \"DUMMY\" } display 'MyConcept'");
	}

	@Test
	public void testConceptNoDefault() throws Exception {
		runTest("Concept",
				new Concept().withCode(new Code().withCode("DEF").withSystem("urn:mysystem")).withDisplay("YourConcept"));
	}
	
	@Test
	public void testIntervalWithDefault() throws Exception {
		runTest("Interval<Integer>",
				new Interval( 10, true, 100, false ),
				"Interval[10,100)" );
	}

	@Test
	public void testIntervalNoDefault() throws Exception {
		runTest("Interval<Integer>",
				new Interval( 99, false, 101, true ) );
	}

	public void runTest(String type, Object expectedOutput) throws Exception {
		runTest(type, expectedOutput, null);
	}

	public void runTest(String type, Object expectedOutput, String defaultValue) throws Exception {
		String cql = "library \"Test\" version '1.0.0'\n";
		cql += "codesystem \"DUMMY\": 'urn:mysystem'\n";
		cql += "parameter Input " + type;
		if (defaultValue != null) {
			cql += " default ";
			cql += defaultValue;
		}
		cql += "\n";
		cql += "define Output:\n\tInput\n";

		Map<String, Object> parameters = null;
		if (defaultValue == null) {
			parameters = new HashMap<>();
			parameters.put("Input", expectedOutput);
		}

		TerminologyProvider tp = new TerminologyProvider() {

			@Override
			public boolean in(Code code, ValueSetInfo valueSet) {
				return false;
			}

			@Override
			public Iterable<Code> expand(ValueSetInfo valueSet) {
				return null;
			}

			@Override
			public Code lookup(Code code, CodeSystemInfo codeSystem) {
				return code;
			}

		};

		LibraryLoader ll = new InMemoryLibraryLoader(Arrays.asList(toLibrary(cql)));
		CqlEngine engine = new CqlEngine(ll, Collections.<String, DataProvider>emptyMap(), tp);
		EvaluationResult result = engine.evaluate("Test", parameters);
		assertEquals(1, result.expressionResults.size());
		Object actual = result.forExpression("Output");

		assertEquals(expectedOutput.toString(), actual.toString());
	}

	public Library toLibrary(String text) throws Exception {
		ModelManager modelManager = new ModelManager();
		LibraryManager libraryManager = new LibraryManager(modelManager);
		CqlTranslator translator = CqlTranslator.fromText(text, modelManager, libraryManager);
		assertEquals(translator.getErrors().toString(), 0, translator.getErrors().size());
		String xml = translator.toXml();
		return CqlLibraryReader.read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}
}
