/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BaseFhirTest;
import com.ibm.cohort.engine.api.service.model.MeasureEvaluation;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfo;
import com.ibm.cohort.engine.helpers.CanonicalHelper;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.parameter.DateParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.watson.common.service.base.ServiceBaseUtility;
import com.ibm.watson.common.service.base.security.Tenant;
import com.ibm.watson.common.service.base.security.TenantManager;
import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Junit class to test the CohortEngineRestHandler.
 */

public class CohortEngineRestHandlerTest extends BaseFhirTest {
	// Need to add below to get jacoco to work with powermockito
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	static {
		PowerMockAgent.initializeIfNeeded();
	}

	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestHandlerTest.class.getName());
	String version;
	String methodName;
	HttpServletRequest requestContext;
	IGenericClient measureClient;
	List<String> httpHeadersList = Arrays.asList("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
	String[] authParts = new String[] { "username", "password" };
	List<MeasureParameterInfo> parameterInfoList = new ArrayList<MeasureParameterInfo>(
			Arrays.asList(new MeasureParameterInfo().documentation("documentation").name("name").min(0).max("max")
					.use("IN").type("type")));

	@Mock
	private static DefaultFhirClientBuilder mockDefaultFhirClientBuilder;
	@Mock
	private static HttpHeaders mockHttpHeaders;
	@Mock
	private static HttpServletRequest mockRequestContext;
	@Mock
	private static Response mockResponse;
	@Mock
	private static ResponseBuilder mockResponseBuilder;
	@InjectMocks
	private static CohortEngineRestHandler restHandler;

	private void prepMocks() {
		prepMocks(true);
	}

	private void prepMocks(boolean prepResponse) {
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		if (prepResponse) {
			PowerMockito.mockStatic(Response.class);
		}
		PowerMockito.mockStatic(TenantManager.class);

		PowerMockito.when(TenantManager.getTenant()).thenReturn(new Tenant() {
			@Override
			public String getTenantId() {
				return "JunitTenantId";
			}

			@Override
			public String getUserId() {
				return "JunitUserId";
			}

		});
		when(mockRequestContext.getRemoteAddr()).thenReturn("1.1.1.1");
		when(mockRequestContext.getLocalAddr()).thenReturn("1.1.1.1");
		when(mockRequestContext.getRequestURL())
				.thenReturn(new StringBuffer("http://localhost:9080/services/cohort/api/v1/evaluation"));
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(httpHeadersList);
		when(mockDefaultFhirClientBuilder.createFhirClient(ArgumentMatchers.any())).thenReturn(null);
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeClass
	public static void setUpHandlerBeforeClass() throws Exception {
		restHandler = new CohortEngineRestHandler();
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ Response.class, TenantManager.class, ServiceBaseUtility.class })
	@Test
	public void testEvaluateMeasureSuccess() throws Exception {
		prepMocks();
		
		Response myResponse = PowerMockito.mock(Response.class);
		
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(Response.class);
		PowerMockito.mockStatic(ResponseBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(Response.status(Response.Status.OK)).thenReturn(mockResponseBuilder);
		PowerMockito.when(mockResponseBuilder.entity(Mockito.any(Object.class))).thenReturn(mockResponseBuilder);
		PowerMockito.when(mockResponseBuilder.header(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(mockResponseBuilder);
		PowerMockito.when(mockResponseBuilder.build())
				.thenReturn(myResponse);
		when(mockResponseBuilder.build()).thenReturn(mockResponse);
		
		assertNotNull( mockResponseBuilder );
		
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
		measure.addLibrary(CanonicalHelper.toCanonicalUrl(library));
		measure.addGroup().addPopulation().setCode(new CodeableConcept().addCoding(new Coding().setSystem("").setCode("cohort"))).setCriteria(new Expression().setExpression("Adult"));
		
		Patient patient = getPatient("patientId", AdministrativeGender.MALE, 40);
		
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockFhirResourceRetrieval(patient);
		
		FhirServerConfig clientConfig = getFhirServerConfig();
		
		Map<String,Parameter> parameterOverrides = new HashMap<>();
		parameterOverrides.put("Measurement Period", new IntervalParameter(
				new DateParameter("2019-07-04")
				, true
				, new DateParameter("2020-07-04")
				, true));
		
		MeasureContext measureContext = new MeasureContext(measure.getId(), parameterOverrides);
		
		MeasureEvaluation evaluationRequest = new MeasureEvaluation();
		evaluationRequest.setDataServerConfig(clientConfig);
		evaluationRequest.setPatientId(patient.getId());
		evaluationRequest.setMeasureContext(measureContext);
		//evaluationRequest.setEvidenceOptions(evidenceOptions);
		
		File tempFile = new File("target/test_measure_v1_0_0.zip");
		try {
			FhirContext fhirContext = FhirContext.forR4();
			IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
			
			TestHelper.createMeasureArtifact(tempFile, parser, measure, library);
			
			// Create the metadata part of the request
			ObjectMapper om = new ObjectMapper();
			String json = om.writeValueAsString(evaluationRequest);
			ByteArrayInputStream jsonIs = new ByteArrayInputStream(json.getBytes());
			IAttachment rootPart = mock(IAttachment.class);
			DataHandler jsonHandler = mock(DataHandler.class);
			when( jsonHandler.getInputStream() ).thenReturn(jsonIs);
			when( rootPart.getDataHandler() ).thenReturn(jsonHandler);
			
			// Create the ZIP part of the request
			IAttachment measurePart = mock(IAttachment.class);
			ByteArrayInputStream zipIs = new ByteArrayInputStream( Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())) );
			DataHandler zipHandler = mock(DataHandler.class);
			when( zipHandler.getInputStream() ).thenReturn(zipIs);
			when( measurePart.getDataHandler() ).thenReturn( zipHandler );
			
			// Assemble them together into a reasonable facsimile of the real request
			IMultipartBody body = mock(IMultipartBody.class);
			when( body.getAttachment(CohortEngineRestHandler.REQUEST_DATA_PART) ).thenReturn(rootPart);
			when( body.getAttachment(CohortEngineRestHandler.MEASURE_PART) ).thenReturn(measurePart);
			
			Response loadResponse = restHandler.evaluateMeasure(mockRequestContext, "version", body);
			assertEquals(mockResponse, loadResponse);
		} finally {
			tempFile.delete();
		}
	}


	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class,
			FHIRRestUtils.class })
	@Test
	public void testGetMeasureParameters() throws Exception {
		prepMocks(false);
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(FHIRRestUtils.class);
		PowerMockito.mockStatic(DefaultFhirClientBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(FHIRRestUtils.parseAuthenticationHeaderInfo(ArgumentMatchers.any())).thenReturn(authParts);
		PowerMockito.when(
				FHIRRestUtils.getFHIRClient(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
						ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(measureClient);
		PowerMockito.when(FHIRRestUtils.getParametersForMeasureIdentifier(ArgumentMatchers.any(),
				ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(parameterInfoList);

		Response loadResponse = restHandler.getMeasureParameters(mockHttpHeaders, "version", "fhirEndpoint",
				"fhirTenantId", "measureIdentifierValue", "measureIdentifierSystem", "measureVersion",
				"fhirTenantIdHeader", "fhirDataSourceIdHeader", "fhirDataSourceId");
		validateParameterResponse(loadResponse);
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ TenantManager.class, ServiceBaseUtility.class, DefaultFhirClientBuilder.class,
			FHIRRestUtils.class })
	@Test
	public void testGetMeasureParametersById() throws Exception {
		prepMocks(false);
		PowerMockito.mockStatic(ServiceBaseUtility.class);
		PowerMockito.mockStatic(FHIRRestUtils.class);
		PowerMockito.mockStatic(DefaultFhirClientBuilder.class);
		PowerMockito.when(ServiceBaseUtility.apiSetup(version, logger, methodName)).thenReturn(null);
		PowerMockito.when(FHIRRestUtils.parseAuthenticationHeaderInfo(ArgumentMatchers.any())).thenReturn(authParts);
		PowerMockito.when(
				FHIRRestUtils.getFHIRClient(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
						ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(measureClient);
		PowerMockito.when(FHIRRestUtils.getParametersForMeasureId(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(parameterInfoList);

		Response loadResponse = restHandler.getMeasureParametersById(mockHttpHeaders, "version", "fhirEndpoint",
				"fhirTenantId", "measureId", "fhirTenantIdHeader", "fhirDataSourceIdHeader", "fhirDataSourceId");
		validateParameterResponse(loadResponse);
	}

	private void validateParameterResponse(Response loadResponse) {
		assertEquals(Status.OK.getStatusCode(), loadResponse.getStatus());
		String validResp = "class MeasureParameterInfoList {\n"
				+ "    parameterInfoList: [class MeasureParameterInfo {\n" + "        name: name\n"
				+ "        use: IN\n" + "        min: 0\n" + "        max: max\n" + "        type: type\n"
				+ "        documentation: documentation\n" + "    ]\n" + "}";
		assertEquals(validResp, loadResponse.getEntity().toString());
	}
}