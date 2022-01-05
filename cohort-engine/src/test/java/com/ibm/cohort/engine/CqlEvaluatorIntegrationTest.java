/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.ibm.cohort.cql.evaluation.ContextNames;
import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;
import com.ibm.cohort.cql.evaluation.CqlEvaluator;
import com.ibm.cohort.cql.evaluation.parameters.DatetimeParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntervalParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opencds.cqf.cql.engine.exception.CqlException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CqlEvaluatorIntegrationTest extends BasePatientTest {

    @Test
    public void testPatientIsFemaleTrue() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);
        CqlEvaluator evaluator = setupTestFor(patient, "cql.basic");
        String expression = "Female";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("Test", "1.0.0", Format.ELM),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testPatientIsFemaleFalse() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);
        CqlEvaluator evaluator = setupTestFor(patient, "cql.basic");
        String expression = "Female";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("Test", "1.0.0", Format.ELM),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, false);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectLibraryVersionSpecified() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.MALE, null);
        CqlEvaluator evaluator = setupTestFor(patient, "cql.basic");
        String expression = "Female";

        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("Test")
                .setVersion("bad-version")
                .setFormat(Format.ELM);

        evaluator.evaluate(
                descriptor,
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
    }

    @Test
    public void testRequiredCQLParameterSpecifiedPatientOutOfRange() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");
        CqlEvaluator evaluator = setupTestFor(patient, "cql.parameters");
        String expression = "Female";

        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put("MaxAge", new IntegerParameter(40));

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestWithParams", "1.0.0", Format.ELM),
                parameters,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, false);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testRequiredCQLParameterSpecifiedPatientInRange() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");
        CqlEvaluator evaluator = setupTestFor(patient, "cql.parameters");
        String expression = "Female";

        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put("MaxAge", new IntegerParameter(50));

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestWithParams", "1.0.0", Format.ELM),
                parameters,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testMissingRequiredCQLParameterNoneSpecified() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");
        CqlEvaluator evaluator = setupTestFor(patient, "cql.parameters");
        String expression = "Female";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestWithParams", "1.0.0", Format.ELM),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, null);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testMissingRequiredCQLParameterSomeSpecified() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");
        CqlEvaluator evaluator = setupTestFor(patient, "cql.parameters");
        String expression = "Female";

        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put("Unused", new IntegerParameter(100));

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestWithParams", "1.0.0", Format.ELM),
                parameters,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, null);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testSimplestHTTPRequestSettings() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);
        FhirServerConfig fhirConfig = getFhirServerConfig();
        CqlEvaluator evaluator = setupTestFor(patient, fhirConfig, "cql.basic");
        String expression = "Female";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("Test", "1.0.0", Format.ELM),
                null,
                new ImmutablePair<>("Patient", "123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
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

        mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", condition);

        FhirServerConfig fhirConfig = getFhirServerConfig();
        CqlEvaluator evaluator = setupTestFor(patient, fhirConfig, "cql.condition", "org.hl7.fhir");

        String expression = "HasActiveCondition";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestStatusActive", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
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
        MappingBuilder builder = get(urlMatching("/Condition\\?(recorded-date=[lg]e.*&){2}subject=Patient%2F123&_format=json"));
        mockFhirResourceRetrieval(builder, condition);

        FhirServerConfig fhirConfig = getFhirServerConfig();
        CqlEvaluator evaluator = setupTestFor(patient, fhirConfig,"cql.condition", "org.hl7.fhir");

        Map<String,Parameter> parameters = new HashMap<>();
        parameters.put("MeasurementPeriod", new IntervalParameter( new DatetimeParameter("1999-01-01T00:00:00-05:00"), true, new DatetimeParameter("2000-01-01T00:00:00-05:00"), false ) );

        String expression = "ConditionInInterval";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestDateQuery", "1.0.0", Format.ELM),
                parameters,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Assert.assertEquals(1, actual.getExpressionResults().size());
        List<Object> value = (List)actual.getExpressionResults().get(expression);
        Assert.assertEquals(1, value.size());
        assertFhirEquals(condition, (IBaseResource)value.get(0));
    }

//    @Test
//    @Ignore // uncomment when JSON ELMs start working -
//            // https://github.com/DBCG/cql_engine/issues/405
//    public void testJsonCQLWithIncludes() throws Exception {
//        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1978-05-06");
//
//        CqlEvaluator wrapper = setupTestFor(patient, "cql/includes/Breast-Cancer-Screening.json");
//
//        final AtomicBoolean found = new AtomicBoolean(false);
//        final AtomicInteger count = new AtomicInteger(0);
//        wrapper.evaluate("Breast-Cancer-Screening", "1", /* parameters= */null, null, Arrays.asList("123"),
//                (p, e, r) -> {
//                    count.incrementAndGet();
//                    if (e.equals("MeetsInclusionCriteria")) {
//                        assertEquals("Unexpected value for expression result", Boolean.TRUE, r);
//                        found.set(true);
//                    }
//                });
//        assertEquals("Missing expression result", true, found.get());
//        verify(1, getRequestedFor(urlEqualTo("/Patient/123?_format=json")));
//    }

    @Test(expected = CqlException.class)
    public void testCannotConnectToFHIRDataServer() throws Exception {
        Patient patient = new Patient();
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);

        FhirServerConfig fhirConfig = new FhirServerConfig();
        fhirConfig.setEndpoint("http://its.not.me");

        CqlEvaluator evaluator = setupTestFor(patient, fhirConfig, "cql.basic");
        evaluator.evaluate(
                newDescriptor("Test", "1.0.0", Format.ELM),
                null,
                newPatientContext("123"),
                Collections.singleton("Female")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLibraryName() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, null);
        CqlEvaluator evaluator = setupTestFor(patient, "cql.basic");
        String expression = "Female";

        evaluator.evaluate(
                newDescriptor("NotCorrect", "1.0.0", Format.ELM),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
    }

    @Test
    public void testUsingUSCoreWithTranslation() throws Exception {
        Patient patient = getPatient("123", AdministrativeGender.MALE, "1983-12-02");

        CqlEvaluator evaluator = setupTestFor(patient, "cql.uscore");
        String expression = "QueryByGender";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestUSCore", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );

        Assert.assertEquals(1, actual.getExpressionResults().size());
        List<Object> value = (List)actual.getExpressionResults().get(expression);
        Assert.assertEquals(1, value.size());
        assertFhirEquals(patient, (IBaseResource)value.get(0));
    }

    /*
     * The following `testUOMEquivalence` tests are intended to document the engine behavior
     * when an author attempts to compare quantities with different UoM values.
     */
    @Test
    public void testUOMEquivalence_demonstrateEqualityIssue() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

        CqlEvaluator evaluator = setupTestFor(patient, "cql.uomequivalence");
        String expression = "IsEqual";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestUOMCompare", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        // when you compare two quantities with different UoM, the
        // the engine returns null.
        expected.put(expression, null);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testUOMEquivalence_demonstrateConversionOnOneSide() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

        CqlEvaluator evaluator = setupTestFor(patient, "cql.uomequivalence");
        String expression = "AreEquivalent";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestUOMCompare", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        // you can use the *convert* function to change the
        // units of a quantity to a known value
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    @Test
    public void testUOMEquivalence_demonstrateConversionOfBothSides() throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

        CqlEvaluator evaluator = setupTestFor(patient, "cql.uomequivalence");
        String expression = "UpConvert";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("TestUOMCompare", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        // The safest thing to do is convert the left and right
        // values to a known, fixed unit
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
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

        Condition condition = new Condition();
        condition.setId("Condition");
        condition.setSubject(new Reference(patient));
        condition.getCode().addCoding().setSystem("SNOMED-CT").setCode("1234");

        // This stub works for [Condition] c where c.code in "ValueSet"
        mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", condition);
        // These stub works for [Condition: "ValueSet"]
        mockFhirResourceRetrieval("/Condition?code=SNOMED-CT%7C1234&subject=Patient%2F123&_format=json", makeBundle(condition));
        mockFhirResourceRetrieval("/Condition?code=SNOMED-CT%7C5678&subject=Patient%2F123&_format=json", makeBundle());

        mockValueSetRetrieval("https://cts.nlm.nih.gov/fhir/ValueSet/1.2.3.4", "SNOMED-CT", "1234");
        mockValueSetRetrieval("https://cts.nlm.nih.gov/fhir/ValueSet/5.6.7.8", "SNOMED-CT", "5678");

        // We have to add a one off mock valueset endpoint for when the engine checks if 1234 is in the 5.6.7.8 valueset
        Parameters response = new Parameters();
        response.addParameter().setValue(new BooleanType(false));
        mockFhirResourceRetrieval("/ValueSet/5.6.7.8/$validate-code?code=1234&system=SNOMED-CT&_format=json", response);

        CqlEvaluator evaluator = setupTestFor(patient, "cql.valueset", "org.hl7.fhir");
        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("Test", "1.0.0", Format.CQL),
                newPatientContext("123")
        );

        Map<String, Object> results = actual.getExpressionResults();
        Assert.assertEquals(5, results.size());
        Assert.assertEquals(true, results.get("RHSRetrieveMatches"));
        Assert.assertEquals(true, results.get("RHSInOperatorMatches"));
        Assert.assertEquals(true, results.get("RHSRetrieveNotMatches"));
        Assert.assertEquals(true, results.get("RHSInOperatorNotMatches"));
        assertFhirEquals(patient, (IBaseResource)results.get("Patient"));
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

    private void runUnsupportedValueSetPropertyTest(String expression) throws Exception {
        Patient patient = getPatient("123", Enumerations.AdministrativeGender.FEMALE, "1983-12-02");

        Condition condition = new Condition();
        condition.setId("Condition");
        condition.setSubject(new Reference(patient));
        condition.getCode().addCoding().setSystem("SNOMED-CT").setCode("1234");

        // This stub works for [Condition] c where c.code in "ValueSet"
        mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", condition);

        CqlEvaluator evaluator = setupTestFor(patient, "cql.valueset", "org.hl7.fhir");

        CqlException ex = assertThrows("Missing expected exception", CqlException.class, () -> {
            evaluator.evaluate(
                    newDescriptor("TestUnsupported", "1.0.0", Format.CQL),
                    null,
                    newPatientContext("123"),
                    Collections.singleton(expression)
            );
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

        CqlEvaluator evaluator = setupTestFor(patient, "cql.temporal");
        String expression = "LHS Starts RHS";

        CqlEvaluationResult actual = evaluator.evaluate(
                newDescriptor("IntervalStartsInterval", "1.0.0", Format.CQL),
                null,
                newPatientContext("123"),
                Collections.singleton(expression)
        );
        Map<String, Object> expected = new HashMap<>();
        expected.put(expression, true);

        Assert.assertEquals(expected, actual.getExpressionResults());
    }

    private void assertFhirEquals(IBaseResource expected, IBaseResource actual) {
        String expectedString = getFhirParser().encodeResourceToString(expected);
        String actualString = getFhirParser().encodeResourceToString(actual);
        Assert.assertEquals(expectedString, actualString);
    }

}
