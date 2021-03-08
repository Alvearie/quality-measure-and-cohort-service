/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfo;
import com.ibm.cohort.engine.measure.RestFhirLibraryResolutionProvider;
import com.ibm.cohort.engine.measure.RestFhirMeasureResolutionProvider;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Junit class to test the FHIRRestUtilsTest.
 */

public class FHIRRestUtilsTest {
	// Need to add below to get jacoco to work with powermockito
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	static {
		PowerMockAgent.initializeIfNeeded();
	}
	IGenericClient measureClient;
	List<String> httpHeadersList = Arrays.asList("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
	List<String> badHttpHeadersList = Arrays.asList("Basic !");
	List<String> noUserPassHttpHeadersList = Arrays.asList("Basic ");
	List<String> emptyStrHttpHeadersList = Arrays.asList("");
	List<String> nullHttpHeadersList = null;
	List<String> emptyHttpHeadersList = new ArrayList<>();
	List<String> justUserNameHttpHeadersList = Arrays.asList("Basic dXNlcm5hbWU=");

	String[] authParts = new String[] { "username", "password" };

	FhirContext ctx = FhirContext.forR4();

	Identifier identifier;

	String testMeasureDef = "{\r\n" + "  \"resourceType\": \"Measure\",\r\n"
			+ "  \"id\": \"wh-cohort-Over-the-Hill-Female-1.0.0-identifier\",\r\n" + "  \"identifier\": [\r\n"
			+ "    {\r\n" + "      \"use\": \"official\",\r\n" + "      \"system\": \"http://fakesystem.org\",\r\n"
			+ "      \"value\": \"999\"\r\n" + "    }\r\n" + "  ],\r\n" + "  \"version\": \"1.0.0\",\r\n"
			+ "  \"name\": \"Over-the-Hill-Female\",\r\n" + "  \"status\": \"active\",\r\n"
			+ "  \"experimental\": true,\r\n" + "  \"publisher\": \"IBM WH Cohorting Test\",\r\n"
			+ "  \"description\": \"Over-the-Hill-Female\",\r\n" + "  \"library\": [\r\n"
			+ "    \"http://ibm.com/fhir/wh-cohort/Library/wh-cohort-Over-the-Hill-Female-1.0.0\"\r\n" + "  ]\r\n"
			+ "}";

	String testMeasureWithDefaultDef = "{\n" +
			"  \"resourceType\": \"Measure\",\n" +
			"  \"id\": \"wh-cohort-Over-the-Hill-Female-1.0.0-identifier\",\n" +
			"  \"identifier\": [\n" +
			"    {\n" +
			"      \"use\": \"official\",\n" +
			"      \"system\": \"http://fakesystem.org\",\n" +
			"      \"value\": \"999\"\n" +
			"    }\n" +
			"  ],\n" +
			"  \"version\": \"1.0.0\",\n" +
			"  \"name\": \"Over-the-Hill-Female\",\n" +
			"  \"status\": \"active\",\n" +
			"  \"experimental\": true,\n" +
			"  \"publisher\": \"IBM WH Cohorting Test\",\n" +
			"  \"description\": \"Over-the-Hill-Female\",\n" +
			"  \"library\": [\n" +
			"    \"http://ibm.com/fhir/wh-cohort/Library/wh-cohort-Over-the-Hill-Female-1.0.0\"\n" +
			"  ],\n" +
			"  \"extension\": [\n" +
			"    {\n" +
			"      \"id\": \"defaultExample\",\n" +
			"      \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
			"      \"valueString\": \"42\"\n" +
			"    }\n" +
			"  ]\n" +
			"}\n";

	String testLibraryDef = "{\r\n" + "  \"resourceType\": \"Library\",\r\n"
			+ "  \"id\": \"wh-cohort-Over-the-Hill-Female-1.0.0\",\r\n"
			+ "  \"url\": \"http://ibm.com/fhir/wh-cohort/Library/wh-cohort-Over-the-Hill-Female-1.0.0\",\r\n"
			+ "  \"version\": \"1.0.0\",\r\n" + "  \"name\": \"Over-the-Hill-Female\",\r\n"
			+ "  \"publisher\": \"IBM WH Cohorting Test\",\r\n" + "  \"description\": \"Over-the-Hill-Female\",\r\n"
			+ "  \"parameter\": [\r\n" + "    {\r\n" + "      \"name\": \"Measurement Period\",\r\n"
			+ "      \"use\": \"in\",\r\n" + "      \"min\": 0,\r\n" + "      \"max\": \"1\",\r\n"
			+ "      \"type\": \"Period\"\r\n" + "    }\r\n" + "  ]\r\n" + "}";

	@Mock
	private static DefaultFhirClientBuilder mockDefaultFhirClientBuilder;
	@Mock
	private static HttpHeaders mockHttpHeaders;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test
	public void testGetFHIRClient() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockDefaultFhirClientBuilder.createFhirClient(ArgumentMatchers.any())).thenReturn(null);

		FHIRRestUtils.getFHIRClient("fhirEndpoint", "userName", "password", "", "fhirTenantId", "", "fhirDataSourceId");

	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test
	public void testGetFHIRClientEmptyHeaders() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockDefaultFhirClientBuilder.createFhirClient(ArgumentMatchers.any())).thenReturn(null);

		FHIRRestUtils.getFHIRClient("fhirEndpoint", "userName", "password", null, "fhirTenantId", null,
				"fhirDataSourceId");

	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test
	public void testParseAuthenticationHeaderInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(httpHeadersList);

		String[] authParts = FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
		assertEquals("username", authParts[0]);
		assertEquals("password", authParts[1]);

	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseEmptyAuthenticationHeaderInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(emptyHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseEmptyAuthenticationHeaderStrInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(emptyStrHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseNoUserPassAuthenticationHeaderStrInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(noUserPassHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseAuthenticationHeaderMissingPasswordInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(justUserNameHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseNUllAuthenticationHeaderInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(nullHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, DefaultFhirClientBuilder.class })
	@Test(expected = IllegalArgumentException.class)
	public void testParseAuthenticationHeaderBadPasswordInfo() throws Exception {

		DefaultFhirClientBuilder mockDefaultFhirClientBuilder = Mockito.mock(DefaultFhirClientBuilder.class);
		PowerMockito.whenNew(DefaultFhirClientBuilder.class).withAnyArguments()
				.thenReturn(mockDefaultFhirClientBuilder);
		when(mockHttpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)).thenReturn(badHttpHeadersList);

		FHIRRestUtils.parseAuthenticationHeaderInfo(mockHttpHeaders);
	}

	@PrepareForTest({ FHIRRestUtils.class, RestFhirLibraryResolutionProvider.class })
	@Test
	public void testGetLibraryParmsForMeasure() throws Exception {

		RestFhirLibraryResolutionProvider mockLibraryResolutionProvider = Mockito
				.mock(RestFhirLibraryResolutionProvider.class);
		PowerMockito.whenNew(RestFhirLibraryResolutionProvider.class).withAnyArguments()
				.thenReturn(mockLibraryResolutionProvider);
		when(mockLibraryResolutionProvider.resolveLibraryByCanonicalUrl(ArgumentMatchers.any()))
				.thenReturn(createLibrary(testLibraryDef));

		Measure measure = createMeasure(testMeasureDef);
		List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getLibraryParmsForMeasure(ArgumentMatchers.any(),
				measure);
		assertEquals(1, parameterInfoList.size());
		MeasureParameterInfo parmInfo = parameterInfoList.get(0);
		assertEquals("Measurement Period", parmInfo.getName());
		assertEquals("In", parmInfo.getUse());
		assertEquals("1", parmInfo.getMax());
		assertEquals("0", parmInfo.getMin().toString());
		assertEquals("Period", parmInfo.getType());

	}

	@PrepareForTest({ FHIRRestUtils.class, RestFhirLibraryResolutionProvider.class,
			RestFhirMeasureResolutionProvider.class })
	@Test
	public void testGetParametersForMeasureId() throws Exception {

		RestFhirLibraryResolutionProvider mockLibraryResolutionProvider = Mockito
				.mock(RestFhirLibraryResolutionProvider.class);
		PowerMockito.whenNew(RestFhirLibraryResolutionProvider.class).withAnyArguments()
				.thenReturn(mockLibraryResolutionProvider);
		RestFhirMeasureResolutionProvider mockMeasureResolutionProvider = Mockito
				.mock(RestFhirMeasureResolutionProvider.class);
		PowerMockito.whenNew(RestFhirMeasureResolutionProvider.class).withAnyArguments()
				.thenReturn(mockMeasureResolutionProvider);
		when(mockLibraryResolutionProvider.resolveLibraryByCanonicalUrl(ArgumentMatchers.any()))
				.thenReturn(createLibrary(testLibraryDef));

		when(mockMeasureResolutionProvider.resolveMeasureById(ArgumentMatchers.any()))
				.thenReturn(createMeasure(testMeasureDef));
		List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getParametersForMeasureId(null, "measureId");
		assertEquals(1, parameterInfoList.size());
		MeasureParameterInfo parmInfo = parameterInfoList.get(0);
		assertEquals("Measurement Period", parmInfo.getName());
		assertEquals("In", parmInfo.getUse());
		assertEquals("1", parmInfo.getMax());
		assertEquals("0", parmInfo.getMin().toString());
		assertEquals("Period", parmInfo.getType());

	}

	@PrepareForTest({ FHIRRestUtils.class, RestFhirLibraryResolutionProvider.class,
			RestFhirMeasureResolutionProvider.class })
	@Test
	public void testGetParametersWithDefaultsForMeasureId() throws Exception {
		RestFhirLibraryResolutionProvider mockLibraryResolutionProvider = Mockito
				.mock(RestFhirLibraryResolutionProvider.class);
		PowerMockito.whenNew(RestFhirLibraryResolutionProvider.class).withAnyArguments()
				.thenReturn(mockLibraryResolutionProvider);
		RestFhirMeasureResolutionProvider mockMeasureResolutionProvider = Mockito
				.mock(RestFhirMeasureResolutionProvider.class);
		PowerMockito.whenNew(RestFhirMeasureResolutionProvider.class).withAnyArguments()
				.thenReturn(mockMeasureResolutionProvider);
		when(mockLibraryResolutionProvider.resolveLibraryByCanonicalUrl(ArgumentMatchers.any()))
				.thenReturn(createLibrary(testLibraryDef));

		when(mockMeasureResolutionProvider.resolveMeasureById(ArgumentMatchers.any()))
				.thenReturn(createMeasure(testMeasureWithDefaultDef));

		List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getParametersForMeasureId(null, "measureId");

		MeasureParameterInfo expectedParamInfo = new MeasureParameterInfo();
		expectedParamInfo.setname("Measurement Period");
		expectedParamInfo.setUse("In");
		expectedParamInfo.setMax("1");
		expectedParamInfo.setMin(0);
		expectedParamInfo.setType("Period");
		expectedParamInfo.setDocumentation(null);

		MeasureParameterInfo otherExpectedParamInfo = new MeasureParameterInfo();
		otherExpectedParamInfo.setname("defaultExample");
		otherExpectedParamInfo.setType("string");
		otherExpectedParamInfo.setDocumentation("Defaults to: 42");

		assertThat(parameterInfoList, containsInAnyOrder(expectedParamInfo, otherExpectedParamInfo));
	}

	/**
	 * Test the successful building of a response.
	 */

	@PrepareForTest({ FHIRRestUtils.class, RestFhirLibraryResolutionProvider.class,
			RestFhirMeasureResolutionProvider.class })
	@Test
	public void testGetParametersForMeasureIdentifier() throws Exception {
		RestFhirLibraryResolutionProvider mockLibraryResolutionProvider = Mockito
				.mock(RestFhirLibraryResolutionProvider.class);
		PowerMockito.whenNew(RestFhirLibraryResolutionProvider.class).withAnyArguments()
				.thenReturn(mockLibraryResolutionProvider);
		RestFhirMeasureResolutionProvider mockMeasureResolutionProvider = Mockito
				.mock(RestFhirMeasureResolutionProvider.class);
		PowerMockito.whenNew(RestFhirMeasureResolutionProvider.class).withAnyArguments()
				.thenReturn(mockMeasureResolutionProvider);
		when(mockLibraryResolutionProvider.resolveLibraryByCanonicalUrl(ArgumentMatchers.any()))
				.thenReturn(createLibrary(testLibraryDef));
		when(mockMeasureResolutionProvider.resolveMeasureByIdentifier(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(createMeasure(testMeasureDef));

		List<MeasureParameterInfo> parameterInfoList = FHIRRestUtils.getParametersForMeasureIdentifier(measureClient,
				identifier, "");
		assertEquals(1, parameterInfoList.size());
		MeasureParameterInfo parmInfo = parameterInfoList.get(0);
		assertEquals("Measurement Period", parmInfo.getName());
		assertEquals("In", parmInfo.getUse());
		assertEquals("1", parmInfo.getMax());
		assertEquals("0", parmInfo.getMin().toString());
		assertEquals("Period", parmInfo.getType());

	}

	private Measure createMeasure(String inputString) throws JsonProcessingException {
		// Instantiate a new parser
		ca.uhn.fhir.parser.IParser parser = ctx.newJsonParser();
		// Parse it
		return parser.parseResource(Measure.class, inputString);
	}

	private Library createLibrary(String inputString) throws JsonProcessingException {
		// Instantiate a new parser
		ca.uhn.fhir.parser.IParser parser = ctx.newJsonParser();
		// Parse it
		return parser.parseResource(Library.class, inputString);
	}

}