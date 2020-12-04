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
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpStatus;
import org.custommonkey.xmlunit.Diff;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.api.service.ServiceBuildConstants;
import com.ibm.watson.common.service.base.utilities.BVT;
import com.ibm.watson.common.service.base.utilities.DVT;
import com.ibm.watson.common.service.base.utilities.ServiceAPIGlobalSpec;
import com.ibm.watson.common.service.base.utilities.UNDERCONSTRUCTION;
import com.ibm.watson.common.service.base.vt.ServiceVTBase;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

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

	@BeforeClass
	public static void setUp() throws Exception {
		// Instantiate service location
		instantiateServiceLocation();
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
	public void testCreateEvaluation() {
		final String RESOURCE = getUrlBase() + CohortServiceAPISpec.CREATE_DELETE_EVALUATION_PATH;
		RequestSpecification request =
			buildBaseRequest(HEADER_CONSUMES_JSON_STANDARD_JSON).
			queryParam("version", ServiceBuildConstants.DATE).body(getJsonFromFile(ServiceAPIGlobalSpec.INP_FOLDER_TYPE,"create_evaluation_inp.json"));

		ValidatableResponse vr = runSuccessValidation(request.post(RESOURCE,getServiceVersion()).then(), ContentType.JSON, HttpStatus.SC_ACCEPTED);
		assertJsonEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"create_evaluation_exp.json"),vr.extract().asString());
		assertJsonStructureEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"create_evaluation_exp.json"),vr.extract().asString());
	}

	@Category(DVT.class) // to tag a specific test to be part of DVT (deployment verification test)
	@Test
	/**
	 * Test the get evaluation status stub
	 *
	 */
	public void testGetEvaluationStatus() {
		final String RESOURCE = getUrlBase() + CohortServiceAPISpec.GET_EVALUATION_STATUS_PATH+"/12345";
		RequestSpecification request =
			buildBaseRequest(HEADER_CONSUMES_JSON_STANDARD_JSON).
			queryParam("version", ServiceBuildConstants.DATE);

		ValidatableResponse vr = runSuccessValidation(request.get(RESOURCE,getServiceVersion()).then(), ContentType.JSON, HttpStatus.SC_OK);
		assertJsonEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"get_evaluation_status_exp.json"),vr.extract().asString());
		assertJsonStructureEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"get_evaluation_status_exp.json"),vr.extract().asString());
	}

	@Category(DVT.class) // to tag a specific test to be part of DVT (deployment verification test)
	@Test
	/**
	 * Test the delete evaluation status stub
	 *
	 */
	public void testDeleteEvaluation() {
		final String RESOURCE = getUrlBase() + CohortServiceAPISpec.CREATE_DELETE_EVALUATION_PATH+"/12345";
		RequestSpecification request =
			buildBaseRequest(HEADER_CONSUMES_JSON_STANDARD_JSON).
			queryParam("version", ServiceBuildConstants.DATE);

		ValidatableResponse vr = runSuccessValidation(request.delete(RESOURCE,getServiceVersion()).then(), ContentType.JSON, HttpStatus.SC_OK);
		assertJsonEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"create_evaluation_exp.json"),vr.extract().asString());
		assertJsonStructureEquals(getJsonFromFile(ServiceAPIGlobalSpec.EXP_FOLDER_TYPE,"create_evaluation_exp.json"),vr.extract().asString());
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
