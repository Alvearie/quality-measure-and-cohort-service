/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Ignore;
import org.junit.Test;
import org.opencds.cqf.cql.engine.exception.CqlException;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.ibm.cohort.engine.parameter.DatetimeParameter;
import com.ibm.cohort.engine.parameter.IntegerParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class CqlEngineWrapperTest extends BasePatientTest {

	@Test
	public void testPatientIsFemaleTrue() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});

		assertEquals(1, count.get());
	}

	@Test
	public void testPatientIsFemaleFalse() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", /* libraryVersion= */null, /* parameters= */null,
				new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testCorrectLibraryVersionSpecified() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	@Ignore
	// TODO: Restore when InMemoryLibraryLoader or whatever becomes production use
	// becomes version aware
	public void testIncorrectLibraryVersionSpecified() throws Exception {

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.MALE);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "9.9.9", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testRequiredCQLParameterSpecifiedPatientOutOfRange() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("MaxAge", new IntegerParameter(40));

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.FALSE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testRequiredCQLParameterSpecifiedPatientInRange() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("MaxAge", new IntegerParameter(50));

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	public void testMissingRequiredCQLParameterNoneSpecified() throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// At the reference date specified in the CQL definition, the
		// Patient will be 30 years old.
		Date birthDate = format.parse("2000-08-01");

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		patient.setBirthDate(birthDate);

		Map<String, Parameter> parameters = null;

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test(expected = Exception.class)
	public void testMissingRequiredCQLParameterSomeSpecified() throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// At the reference date specified in the CQL definition, the
		// Patient will be 30 years old.
		Date birthDate = format.parse("2000-08-01");

		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		patient.setBirthDate(birthDate);

		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("Unused", new IntegerParameter(100));

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", parameters, new HashSet<>(Arrays.asList("Female")), Arrays.asList("123"),
				(patientId, expression, result) -> {
					count.incrementAndGet();
					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testSimplestHTTPRequestSettings() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		FhirServerConfig fhirConfig = getFhirServerConfig();
		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Test", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("Female")),
				Arrays.asList("123"), (patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("Female", expression);
					assertEquals(Boolean.TRUE, result);
				});
		assertEquals(1, count.get());
	}

	@Test
	public void testConditionClinicalStatusActiveIsMatched() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		Condition condition = new Condition();
		condition.setId("condition");
		condition.setSubject(new Reference("Patient/123"));
		condition
				.setClinicalStatus(new CodeableConcept()
						.addCoding(new Coding().setCode("active")
								.setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical"))
						.setText("Active"));

		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", condition);

		FhirServerConfig fhirConfig = getFhirServerConfig();
		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,
				"cql/condition/test-status-active.cql");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", "1.0.0", /* parameters= */null,
				new HashSet<>(Arrays.asList("HasActiveCondition")), Arrays.asList("123"),
				new ProxyingEvaluationResultCallback((patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("HasActiveCondition", expression);
					assertEquals(Boolean.TRUE, result);
				}));
		assertEquals(1, count.get());
	}
	
	@Test
	public void testConditionDateRangeCriteriaMatched() throws Exception {

		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sdf.parse("2000-01-01");
		
		Condition condition = new Condition();
		condition.setId("condition");
		condition.setSubject(new Reference("Patient/123"));
		condition.setRecordedDate( date );

		// Wiremock does not support request matching withQueryParam() function does not support
		// the same parameter multiple times, so we do some regex work and try to make it 
		// somewhat order independent while still readable.
		// @see https://github.com/tomakehurst/wiremock/issues/398
		MappingBuilder builder = get(urlMatching("/Condition\\?(recorded-date=[lg]e.*&){2}subject=Patient%2F123"));
		mockFhirResourceRetrieval(builder, condition);

		FhirServerConfig fhirConfig = getFhirServerConfig();
		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig,
				"cql/condition/test-date-query.xml");
		
		Map<String,Parameter> parameters = new HashMap<>();
		parameters.put("MeasurementPeriod", new IntervalParameter( new DatetimeParameter("1999-01-01T00:00:00-05:00"), true, new DatetimeParameter("2000-01-01T00:00:00-05:00"), false ) );

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", "1.0.0", parameters,
				new HashSet<>(Arrays.asList("ConditionInInterval")), Arrays.asList("123"),
				new ProxyingEvaluationResultCallback((patientId, expression, result) -> {
					count.incrementAndGet();

					assertEquals("ConditionInInterval", expression);
					//assertEquals(Boolean.TRUE, result);
				}));
		assertEquals(1, count.get());
	}



	@Test
	public void testNumCallsUsingEngineWrapperMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", null, /* parameters= */null, null, Arrays.asList("123"),
				new ProxyingEvaluationResultCallback((p, e, r) -> {
					count.incrementAndGet();
					System.out.println("Expression: " + e);
					System.out.println("Result: " + r);
				}));
		assertEquals(4, count.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	public void testNumCallsUsingPerDefineMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");

		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateExpressionByExpression("Test", null, /* parameters= */null, null, Arrays.asList("123"),
				new ProxyingEvaluationResultCallback((p, e, r) -> {
					count.incrementAndGet();
					System.out.println("Expression: " + e);
					System.out.println("Result: " + r);
				}));
		assertEquals(4, count.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	public void testNumCallsWithParamsUsingEngineWrapperMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("MaxAge", new IntegerParameter(40));

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateWithEngineWrapper("Test", null, parameters, null, Arrays.asList("123"), new ProxyingEvaluationResultCallback((p, e, r) -> {
			count.incrementAndGet();
			if (e.equals("ParamMaxAge")) {
				assertEquals("Unexpected value for expression result", "40", r);
				found.set(true);
			}
		}));
		assertEquals("Missing expression result", true, found.get());

		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	public void testNumCallsWithParamsUsingPerDefineMethod() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/parameters/test-with-params.xml");

		Map<String, Parameter> parameters = new HashMap<>();
		parameters.put("MaxAge", new IntegerParameter(40));

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluateExpressionByExpression("Test", null, parameters, null, Arrays.asList("123"), new ProxyingEvaluationResultCallback((p, e, r) -> {
			count.incrementAndGet();
			if (e.equals("ParamMaxAge")) {
				assertEquals("Unexpected value for expression result", "40", r);
				found.set(true);
			}
		}));
		assertEquals("Missing expression result", true, found.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test
	@Ignore // uncomment when JSON starts working -
			// https://github.com/DBCG/cql_engine/issues/405
	public void testJsonCQLWithIncludes() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/includes/Breast-Cancer-Screening.json");

		final AtomicBoolean found = new AtomicBoolean(false);
		final AtomicInteger count = new AtomicInteger(0);
		wrapper.evaluate("Breast-Cancer-Screening", "1", /* parameters= */null, null, Arrays.asList("123"),
				(p, e, r) -> {
					count.incrementAndGet();
					if (e.equals("MeetsInclusionCriteria")) {
						assertEquals("Unexpected value for expression result", Boolean.TRUE, r);
						found.set(true);
					}
				});
		assertEquals("Missing expression result", true, found.get());
		verify(1, getRequestedFor(urlEqualTo("/Patient/123")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidWrapperSetup() throws Exception {
		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		wrapper.evaluate("Test", null, null, null, Arrays.asList("123"), (p, e, r) -> {
			/* do nothing */ });
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingRequiredInputParameters() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/basic/test.xml");
		wrapper.evaluate(null, null, null, null, null, null);
	}

	@Test(expected = Exception.class)
	public void testCannotConnectToFHIRDataServer() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		FhirServerConfig fhirConfig = new FhirServerConfig();
		fhirConfig.setEndpoint("http://its.not.me");

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");
		wrapper.evaluate("Test", /* version= */null, /* parameters= */null, /* expressions= */null,
				Arrays.asList("123"), (p, e, r) -> {
					fail("Execution should not reach here");
				});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidLibraryName() throws Exception {
		Patient patient = new Patient();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);

		FhirServerConfig fhirConfig = getFhirServerConfig();

		CqlEngineWrapper wrapper = setupTestFor(patient, fhirConfig, "cql/basic/test.xml");
		wrapper.evaluate("NotCorrect", /* version= */null, /* parameters= */null, /* expressions= */null,
				Arrays.asList("123"), (p, e, r) -> {
					fail("Execution should not reach here");
				});
	}
	
	@Test
	public void testUsingUSCoreELMSuccessfulExecution() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

		final AtomicInteger resultCount = new AtomicInteger(0);
		// Using pre-compiled ELM that is correctly formatted for consumption. There is a test
		// case below that does the same thing with translation.
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/uscore/test-uscore.xml");
		wrapper.evaluate("Test", /* version= */null, /* parameters= */null, new HashSet<>(Arrays.asList("QueryByGender")),
				Arrays.asList("123"), (p, e, r) -> {
					assertEquals("QueryByGender", e);
					resultCount.incrementAndGet(); 
				});
		assertEquals(1, resultCount.get());
	}
	
	@Test
	@Ignore
	// If you try to compile CQL using USCore 3.0.1 with the latest translator it will blow up. 
	// @see https://github.com/DBCG/cql_engine/issues/424
	public void testUsingUSCoreWithTranslationSuccessfulExecution() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

		final AtomicInteger resultCount = new AtomicInteger(0);
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/uscore/test-uscore.cql");
		wrapper.evaluate("Test", /* version= */null, /* parameters= */null, new HashSet<>(Arrays.asList("QueryByGender")),
				Arrays.asList("123"), (p, e, r) -> {
					assertEquals("QueryByGender", e);
					resultCount.incrementAndGet(); 
				});
		assertEquals(1, resultCount.get());
	}

	@Test
	/**
	 * This test exists to document the engine behavior when an author attempts to compare
	 * quantities with different UoM values. 
	 * @throws Exception on any error.
	 */
	public void testUOMEquivalence() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

		final AtomicInteger resultCount = new AtomicInteger(0);
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/uomequivalence/TestUOMCompare-1.0.0.cql");
		wrapper.evaluate("TestUOMCompare", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("IsEqual", "AreEquivalent", "UpConvert")),
				Arrays.asList(patient.getId()), (p, e, r) -> {
					if( e.equals("IsEqual") ) {
						// when you compare two quantities with different UoM, the
						// the engine returns null.
						assertEquals(null, r);
					} else if( e.equals( "AreEquivalent") ) {
						// you can use the *convert* function to change the
						// units of a quantity to a known value
						assertEquals(Boolean.TRUE, r); 
					} else if( e.equals( "UpConvert") ) {
						// Or, the safest thing to do is convert the left and right
						// values to a known, fixed unit
						assertEquals(Boolean.TRUE, r);
					}
					resultCount.incrementAndGet();
				});
		assertEquals(3, resultCount.get());
	}
	
	@Test
	/**
	 * This test exists to validate the the engine correctly expands a valueset
	 * and correctly determines resources that overlap the valueset membership.
	 *  
	 * @throws Exception on any error.
	 */
	public void testValueSetMembership() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

		final AtomicInteger resultCount = new AtomicInteger(0);
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/valueset/Test-1.0.0.cql");
		
		Condition condition = new Condition();
		condition.setId("Condition");
		condition.setSubject(new Reference(patient));
		condition.getCode().addCoding().setSystem("SNOMED-CT").setCode("1234");

		// This stub works for [Condition] c where c.code in "ValueSet"
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", condition);
		// These stub works for [Condition: "ValueSet"]
		mockFhirResourceRetrieval("/Condition?code=SNOMED-CT%7C1234&subject=Patient%2F123", makeBundle(condition));
		mockFhirResourceRetrieval("/Condition?code=SNOMED-CT%7C5678&subject=Patient%2F123", makeBundle());
		
		mockValueSetRetrieval("https://cts.nlm.nih.gov/fhir/ValueSet/1.2.3.4", "SNOMED-CT", "1234");
		mockValueSetRetrieval("https://cts.nlm.nih.gov/fhir/ValueSet/5.6.7.8", "SNOMED-CT", "5678");
		
		wrapper.evaluate("Test", "1.0.0", /* parameters= */null, null,
				Arrays.asList(patient.getId()), (p, e, r) -> {
					if( e.endsWith("NotExists") ) {
						assertEquals(Boolean.FALSE, r);
					} else if( e.endsWith( "Exists") ) {
						// you can use the *convert* function to change the
						// units of a quantity to a known value
						assertEquals(Boolean.TRUE, r); 
					}
					resultCount.incrementAndGet();
				});
		// The four checks + the patient
		assertEquals(5, resultCount.get());
	}
	
	@Test
	public void testUnsupportedValueSetVersionFeature() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesVersionVSInOperator");
	}
	
	@Test
	public void testUnsupportedValueSetCodeSystemsFeature() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesCodeSystemsVSInOperator");
	}
	
	@Test
	public void testUnsupportedValueSetFeaturesCombined() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesBothVSInOperator");
	}

	@Test
	@Ignore // waiting on fix in RestFhirRetrieveProvider	
	public void testUnsupportedValueSetVersionFeatureFilteredRetrieve() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesVersionVS");
	}
	
	@Test
	@Ignore // waiting on fix in RestFhirRetrieveProvider
	public void testUnsupportedValueSetCodeSystemsFeatureFilteredRetrieve() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesCodeSystemsVS");
	}
	
	@Test
	@Ignore // waiting on fix in RestFhirRetrieveProvider
	public void testUnsupportedValueSetFeaturesCombinedFilteredRetrieve() throws Exception {
		runUnsupportedValueSetPropertyTest("UsesBothVS");
	}

	private void runUnsupportedValueSetPropertyTest(String expression) throws ParseException, Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

		Condition condition = new Condition();
		condition.setId("Condition");
		condition.setSubject(new Reference(patient));
		condition.getCode().addCoding().setSystem("SNOMED-CT").setCode("1234");

		// This stub works for [Condition] c where c.code in "ValueSet"
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123", condition);		
		
		final AtomicInteger resultCount = new AtomicInteger(0);
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/valueset/TestUnsupported-1.0.0.cql");
		
		CqlException ex = assertThrows("Missing expected exception", CqlException.class, () -> {
				wrapper.evaluate("TestUnsupported", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList(expression)),
						Arrays.asList(patient.getId()), (p, e, r) -> {
							resultCount.incrementAndGet();
						});
			});
		assertTrue( "Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("version and code system bindings are not supported at this time") );
	}
	
	@Test
	/**
	 * This test exists to validate the the engine correctly evaluates CQL
	 * that includes an "interval starts interval" expression. This was 
	 * called out because the CQL Author's Guide documentation mentioned
	 * "interval begins interval" as a supported feature and "begins"
	 * isn't the correct operator name. 
	 *  
	 * @throws Exception on any error.
	 */
	public void testIntervalStartsInterval() throws Exception {
		Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");
		
		final AtomicInteger resultCount = new AtomicInteger(0);
		CqlEngineWrapper wrapper = setupTestFor(patient, "cql/temporal/IntervalStartsInterval-1.0.0.cql");
		
		wrapper.evaluate("IntervalStartsInterval", "1.0.0", /* parameters= */null, new HashSet<>(Arrays.asList("LHS Starts RHS")),
				Arrays.asList(patient.getId()), (p, e, r) -> {
					assertEquals(Boolean.TRUE, r);
					resultCount.incrementAndGet();
				});
		assertEquals(1, resultCount.get());
	}
}
