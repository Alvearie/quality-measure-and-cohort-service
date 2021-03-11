/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service.vt; 

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonStructureEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLNotEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathValuesEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathValuesNotEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathsEqual;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathsNotEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.custommonkey.xmlunit.Diff;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.ibm.cohort.engine.api.service.CohortEngineRestHandler;
import com.ibm.cohort.engine.api.service.ServiceBuildConstants;
import com.ibm.cohort.engine.api.service.TestHelper;
import com.ibm.cohort.engine.api.service.model.DateParameter;
import com.ibm.cohort.engine.api.service.model.IntervalParameter;
import com.ibm.cohort.engine.api.service.model.MeasureEvaluation;
import com.ibm.cohort.engine.api.service.model.Parameter;
import com.ibm.cohort.engine.helpers.CanonicalHelper;
import com.ibm.cohort.engine.measure.evidence.MeasureEvidenceOptions;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.watson.common.service.base.utilities.BVT;
import com.ibm.watson.common.service.base.utilities.DVT;
import com.ibm.watson.common.service.base.utilities.ServiceAPIGlobalSpec;
import com.ibm.watson.common.service.base.utilities.UNDERCONSTRUCTION;
import com.ibm.watson.common.service.base.vt.ServiceVTBase;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Headers;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;

/*
 * PLEASE READ THIS FIRST
 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 *
 * Down this class you shall see some basic samples of using JSONUNIT and XMLUNIT to show case XML or JSON object/
 * string comparison. Recommended way of comparing the expected vs actual JSON or XML responses.
 *
 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 *
 * ALSO note the usage of Categories @Category BVT and DVT and they can be tagged at class level OR method level.
 * 1. ALL classes that need to be run as BVT or regression should be tagged @Category(BVT.class)
 * 2. ALL  classes that need to be run as DVT should be tagged @Category(DVT.class)
 *
 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 *
 * FINALLY, note you can get the required information related to the deployment ENV using below api calls
 * getUrlBase()
 *
 */

@Category(BVT.class) 			// all classes must be tagged as BVT, otherwise will not be picked up by automation runs
public class DefaultVT extends ServiceVTBase {
	private static final Logger logger = LoggerFactory.getLogger(DefaultVT.class.getName());

	private static FhirServerConfig dataServerConfig;
	private static FhirServerConfig termServerConfig;
	
	@BeforeClass
	public static void setUp() throws Exception {
		// Instantiate service location
		instantiateServiceLocation();
		
		if( System.getProperty("test.host") == null ) {
			System.setProperty("test.host", "localhost");
		}
		
		if(  System.getProperty("test.httpPort") == null ) { 
			System.setProperty("test.httpPort", "9080");
		}
		
		if( System.getProperty("test.contextRoot") == null ) {
			System.setProperty("test.contextRoot", "services/cohort");
		}
		
		ObjectMapper om = new ObjectMapper();
		
		String dataClientConfigPath = System.getProperty("test.dataConfig");
		if( dataClientConfigPath != null ) {
			File dataClientConfigFile = new File(dataClientConfigPath);
			dataServerConfig = om.readValue(dataClientConfigFile, FhirServerConfig.class);
		} else { 
			fail("Missing required system property 'test.dataConfig'");
		}
		
		String termClientConfigPath = System.getProperty("test.terminologyConfig");
		if( termClientConfigPath != null ) {
			File termClientConfigFile = new File(termClientConfigPath);
			termServerConfig = om.readValue(termClientConfigFile, FhirServerConfig.class);
		} else {
			termServerConfig = dataServerConfig;
		}
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// Run code after all test cases complete
		JsonAssert.resetOptions();
	}

	@Category(DVT.class) // to tag a specific test to be part of DVT (deployment verification test)
	@Test
	/**
	 * Test the create evaluation status stub
	 *
	 */
	public void testMeasureEvaluation() throws Exception {
		final String RESOURCE = getUrlBase() + CohortServiceAPISpec.CREATE_DELETE_EVALUATION_PATH;
		
		Calendar c = Calendar.getInstance();
		c.set(2019, 07, 04, 0, 0, 0);
		Date startDate = c.getTime();
		
		c.add( Calendar.YEAR, +1);
		Date endDate = c.getTime();
		
		FhirContext fhirContext = FhirContext.forR4();
		IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
		
		Library library = new Library();
		library.setId("libraryId");
		library.setName("test_library");
		library.setVersion("1.0.0");
		library.setUrl("http://ibm.com/health/Library/test_library");
		library.addContent().setContentType("text/cql").setData("library test_library version '1.0.0'\nparameter AgeOfMaturation default 18\nusing FHIR version '4.0.0'\ncontext Patient\ndefine Adult: AgeInYears() >= \"AgeOfMaturation\"".getBytes());
		
		Measure measure = new Measure();
		measure.setId("measureId");
		measure.setName("test_measure");
		measure.setVersion("1.0.0");
		measure.setUrl("http://ibm.com/health/Measure/test_measure");
		measure.setScoring(new CodeableConcept().addCoding(new Coding().setCode(MeasureScoring.COHORT.toCode())));
		measure.setEffectivePeriod(new Period().setStart(startDate).setEnd(endDate));
		measure.addLibrary(CanonicalHelper.toCanonicalUrl(library));
		measure.addGroup().addPopulation().setCode(new CodeableConcept().addCoding(new Coding().setSystem("http://hl7.org/fhir/measure-population").setCode("initial-population"))).setCriteria(new Expression().setExpression("Adult"));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TestHelper.createMeasureArtifact(baos, parser, measure, library);
		
		Files.write( baos.toByteArray(), new File("c:/Dev/cdt-config/test_measure_v1_0_0.zip"));
		
		MeasureEvaluation requestData = new MeasureEvaluation();
		requestData.setDataServerConfig(dataServerConfig);
		requestData.setTerminologyServerConfig(termServerConfig);
		requestData.setPatientId("eb068c3f-3954-50c6-0c67-2b82d29865f0");
		requestData.setMeasureId(measure.getId());
		requestData.setEvidenceOptions(new MeasureEvidenceOptions(false,false));
		
		Parameter measurementPeriod =  new IntervalParameter("Measurement Period"
				, new DateParameter("start", "2019-07-04")
				, true
				, new DateParameter("end", "2020-07-04")
				, true);
		Map<String,Parameter> parameterOverrides = new HashMap<>();
		parameterOverrides.put(measurementPeriod.getName(), measurementPeriod);
		requestData.setParameterOverrides(parameterOverrides);
		
		ObjectMapper om = new ObjectMapper();
		System.out.println( om.writeValueAsString(requestData) );
		
		RequestSpecification request = buildBaseRequest(new Headers())
				.queryParam("version", ServiceBuildConstants.DATE)
				.multiPart(CohortEngineRestHandler.REQUEST_DATA_PART, requestData, "application/json")
				.multiPart(CohortEngineRestHandler.MEASURE_PART, "test_measure_v1_0_0.zip", new ByteArrayInputStream(baos.toByteArray()));
		
		ValidatableResponse response = request.post(RESOURCE,getServiceVersion()).then();
		ValidatableResponse vr = runSuccessValidation(response, ContentType.JSON, HttpStatus.SC_OK);
		
		String expected = getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"measure_evaluation_exp.json");
		String actual = vr.extract().asString();
		
		MeasureReport expectedReport = parser.parseResource(MeasureReport.class, expected);
		MeasureReport actualReport = parser.parseResource(MeasureReport.class, actual);
		
		// The order is not guaranteed and the Measure ID changes between runs
		assertEquals( expectedReport.getEvaluatedResource().size(), actualReport.getEvaluatedResource().size() );
		expectedReport.setEvaluatedResource(Collections.emptyList());
		actualReport.setEvaluatedResource(Collections.emptyList());
		
		// The period has the timezone encoded, so we can't use a point-in-time
		// snapshot to evaluate equality. The point-in-time might have a different
		// timezone offset than the actual value. This will ignore timezone.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals( sdf.format(expectedReport.getPeriod().getStart()), sdf.format(actualReport.getPeriod().getStart()));
		assertEquals( sdf.format(expectedReport.getPeriod().getEnd()), sdf.format(actualReport.getPeriod().getEnd()));
		expectedReport.setPeriod(null);
		actualReport.setPeriod(null);
		
		// The remainder should match naturally
		assertJsonEquals(parser.encodeResourceToString(expectedReport),parser.encodeResourceToString(actualReport));
	}

	@Test
	public void testJsonunitAssertSample() {
		assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", "{\"test\":{\"a\":1,\"b\":2,\"c\":3}}");
		assertJsonStructureEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}", "{\"test\":{\"a\":1,\"b\":2,\"c\":3}}");
	}

    /*
     * Match and Assert a specific section or node of JsonObject to
     * ignore fields with null values
     *
     */

    @Test
    public void testIf_responseJsonSpecificNode_ValueNullsAreIgnored() {
            JsonAssert.setOptions(Option.TREATING_NULL_AS_ABSENT);
            assertJsonEquals("{\"test\":{\"a\":1}}",
                 "{\"test\":{\"a\":1, \"b\": null}}");
    }

    /*
     * Match and Assert a specific section or node of JsonObject to
     * ignore values and extra fields
     *
     */

    @Test
    public void testIf_responseJson_IgnoreValuesAndFields() {
            JsonAssert.setOptions(IGNORING_VALUES,Option.IGNORING_EXTRA_FIELDS);
            assertJsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}",
            "{\"test\":{\"a\":3,\"b\":2,\"c\":1,\"d\":4}}");
    }


	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 *
	 * Below are few samples on how to use XMLUnit asserts to compare
	 * XML documents.
	 *
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 *
	 */

	@Category(UNDERCONSTRUCTION.class) // tag as mentioned, to ignore any tests to be picked up from continuous integration/automation
	@Test
	public void testXMLUnitAssertForEquality() throws Exception {
        String myControlXML = "<msg><uuid>0x00435A8C</uuid></msg>";
        String myTestXML = "<msg><uuid>0x00435A8C</uuid></msg>";
        String myTestXMLDIFFER = "<msg><localId>2376</localId></msg>";

        assertXMLEqual("comparing test xml to control xml", myControlXML, myTestXML);

        assertXMLNotEqual("test xml differ not similar to control xml", myControlXML, myTestXMLDIFFER);
    }


	@Test
    public void testXMLUnitAssertIdenticalvsSimilar() throws Exception {
        String myControlXML = "<struct><int>3</int><boolean>false</boolean></struct>";
        String myTestXMLIDENTICAL = "<struct><int>3</int><boolean>false</boolean></struct>";
        String myTestXMLSIMILAR = "<struct><boolean>false</boolean><int>3</int></struct>";

        Diff myDiff = new Diff(myControlXML, myTestXMLSIMILAR);
        assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());

        myDiff = new Diff(myControlXML, myTestXMLIDENTICAL);
        assertTrue("but are they identical? " + myDiff, myDiff.identical());
    }


	@Test
    public void testXMLUnitAssertXPaths() throws Exception {
        String mySolarSystemXML = "<solar-system><planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";
        assertXpathExists("//planet[@name='Earth']", mySolarSystemXML);
        assertXpathNotExists("//star[@name='alpha centauri']", mySolarSystemXML);
        assertXpathsEqual("//planet[@name='Earth']", "//planet[@position='3']", mySolarSystemXML);
        assertXpathsNotEqual("//planet[@name='Venus']", "//planet[@supportsLife='yes']", mySolarSystemXML);
    }


	@Test
    public void testXMLUnitAssertXPathValues() throws Exception {
        String myJavaFlavours = "<java-flavours><jvm current='some platforms'>1.1.x</jvm>"
            + "<jvm current='no'>1.2.x</jvm><jvm current='yes'>1.3.x</jvm>"
            + "<jvm current='yes' latest='yes'>1.4.x</jvm></java-flavours>";
        assertXpathEvaluatesTo("1.4.x", "//jvm[@latest='yes']", myJavaFlavours);
        assertXpathEvaluatesTo("2", "count(//jvm[@current='yes'])", myJavaFlavours);
        assertXpathValuesEqual("//jvm[4]/@latest", "//jvm[4]/@current", myJavaFlavours);
        assertXpathValuesNotEqual("//jvm[2]/@current", "//jvm[3]/@current", myJavaFlavours);
    }

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
