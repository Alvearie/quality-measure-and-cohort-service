/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.ParameterDefinition;
import org.hl7.fhir.r4.model.ParameterDefinition.ParameterUse;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import com.ibm.cohort.engine.DefaultFhirClientBuilder;
import com.ibm.cohort.engine.IBMFhirServerConfig;
import com.ibm.cohort.engine.api.service.model.MeasureParameterInfo;
import com.ibm.cohort.engine.measure.RestFhirLibraryResolutionProvider;
import com.ibm.cohort.engine.measure.RestFhirMeasureResolutionProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * A set of methods that can be re-used across REST calls
 *
 */
public class FHIRRestUtils {

	
	/**
	 * @param fhirEndpoint
	 * @param userName
	 * @param password
	 * @param fhirTenantIdHeader
	 * @param fhirTenantId
	 * @param fhirDataSourceIdHeader
	 * @param fhirDataSourceId
	 * @return IGenericClient
	 * 
	 * Convenience method to get a FHIR client
	 */
	public static IGenericClient getFHIRClient(String fhirEndpoint, String userName, String password, String fhirTenantIdHeader, String fhirTenantId, String fhirDataSourceIdHeader, String fhirDataSourceId) {
		IBMFhirServerConfig config = new IBMFhirServerConfig();
		config.setEndpoint(fhirEndpoint);
		config.setUser(userName);
		config.setPassword(password);

		if(fhirTenantIdHeader == null || fhirTenantIdHeader.trim().isEmpty()) {
			fhirTenantIdHeader = IBMFhirServerConfig.DEFAULT_TENANT_ID_HEADER;
		}
		config.setTenantIdHeader(fhirTenantIdHeader);
		config.setTenantId(fhirTenantId);

		if(fhirDataSourceIdHeader == null || fhirDataSourceIdHeader.trim().isEmpty()) {
			fhirDataSourceIdHeader = IBMFhirServerConfig.DEFAULT_DATASOURCE_ID_HEADER;
		}
		config.setDataSourceIdHeader(fhirDataSourceIdHeader);
		config.setDataSourceId(fhirDataSourceId);

		FhirContext ctx = FhirContext.forR4();
		DefaultFhirClientBuilder builder = new DefaultFhirClientBuilder(ctx);
		
		IGenericClient fhirClient = builder.createFhirClient(config);
		
		return fhirClient;
	}
	
	/**
	 * @param measureClient
	 * @param identifier
	 * @param measureVersion
	 * @return List<MeasureParameterInfo>
	 * 
	 * Get a list of MeasureParameterInfo objects which are used to return results via the REST API.
	 * Objects describe the parameters for libraries linked to by the Measure resource
	 * if measureVersion is an empty string, the underlying code will attempt to resolve the latest
	 * measure version using semantic versioning
	 */
	public static List<MeasureParameterInfo> getParametersForMeasureIdentifier(IGenericClient measureClient, Identifier identifier, String measureVersion) {
		RestFhirMeasureResolutionProvider msp = new RestFhirMeasureResolutionProvider(measureClient);
		Measure measure = msp.resolveMeasureByIdentifier(identifier, measureVersion);

		return FHIRRestUtils.getLibraryParmsForMeasure(measureClient, measure);
	}
	
	/**
	 * @param measureClient
	 * @param measureId
	 * @return List<MeasureParameterInfo>
	 * 
	 * Get a list of MeasureParameterInfo objects which are used to return results via the REST API.
	 * Objects describe the parameters for libraries linked to by the FHIR Measure resource id
	 */
	public static List<MeasureParameterInfo> getParametersForMeasureId(IGenericClient measureClient, String measureId) {
		RestFhirMeasureResolutionProvider msp = new RestFhirMeasureResolutionProvider(measureClient);
		Measure measure = msp.resolveMeasureById(measureId);

		return FHIRRestUtils.getLibraryParmsForMeasure(measureClient, measure);
	}
	
	/**
	 * @param measureClient
	 * @param measure
	 * @return List<MeasureParameterInfo>
	 * 
	 * Get a list of MeasureParameterInfo objects which are used to return results via the REST API.
	 * Objects describe the parameters for libraries linked to by the given FHIR Measure object
	 */
	protected static List<MeasureParameterInfo> getLibraryParmsForMeasure(IGenericClient measureClient, Measure measure){
		List<MeasureParameterInfo> parameterInfoList = new ArrayList<MeasureParameterInfo>();
		LibraryResolutionProvider<Library> libraryResolutionProvider = new RestFhirLibraryResolutionProvider(
				measureClient);

		//get the library urls for the libraries linked to this measure
		List<CanonicalType> measureLibs = measure.getLibrary();

		//loop through listed libraries and get their parameter info
		for(CanonicalType libraryCanonType : measureLibs) {
			String libraryUrl = libraryCanonType.getValue();
			Library library = libraryResolutionProvider.resolveLibraryByCanonicalUrl(libraryUrl);

			if(library == null) {
				// if we can resolve by url, try to resolve by fhir library.id
				String libraryId = libraryCanonType.getId();
				if(libraryId != null) {
					library = libraryResolutionProvider.resolveLibraryById(libraryId);
				}
			}

			if(library != null) {
				//get the parameter info and add it to our list
				List<ParameterDefinition> parmDefs = library.getParameter();
				if(parmDefs != null) {
					for(ParameterDefinition parmDef : parmDefs) {
						//use describes input/outut parameter type
						ParameterUse use = parmDef.getUse();
						String useStr = null;
						if(use != null) {
							useStr = use.getDisplay();
						}
						//min/max describe how many times a parameter can/must be used
						MeasureParameterInfo parmInfo = new MeasureParameterInfo().name(parmDef.getName())
						.type(parmDef.getType())
						.min(parmDef.getMin())
						.max(parmDef.getMax())
						.use(useStr)
						.documentation(parmDef.getDocumentation());
						parameterInfoList.add(parmInfo);
					}
				}
			}
		}
		return parameterInfoList;
	}
	
	/**
	 * @param httpHeaders
	 * @return String[] containing username as first element and password as second
	 * @throws IllegalArgumentException
	 */
	public static String[] parseAuthenticationHeaderInfo(HttpHeaders httpHeaders) throws IllegalArgumentException {
		List<String> headers = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
		if(headers == null || headers.isEmpty()) {
			throw new IllegalArgumentException("Missing HTTP authorization header information. FHIR server credentials must be passed using HTTP Basic Authentication");
		}
		String authString = headers.get(0);
		if(authString == null || authString.trim().isEmpty()) {
			throw new IllegalArgumentException("No data in HTTP authorization header. FHIR server credentials must be passed using HTTP Basic Authentication");
		}
		//HTTP basic authentication headers are passed in as a base64 encoded string so we need to decode it
		String decodedAuthStr = FHIRRestUtils.decodeAuthenticationString(authString);
		//returned string will be in format of username:password
		String[] authParts = decodedAuthStr.split(":");
		if(authParts.length < 2) {
			throw new IllegalArgumentException("Missing data in HTTP authorization header. FHIR server credentials must be passed using HTTP Basic Authentication");
		}
		
		return authParts;
	}
	
	/**
	 * @param authString a base64 encoded HTTP basic authentication header string
	 * @return decoded string value
	 */
	public static String decodeAuthenticationString(String authString) {
		String decodedAuth = "";
		// Basic authentication header is in the format "Basic 5tyc0uiDat4" using base 64 encoding
		// We need to extract data before decoding it back to original string
		try {
			String[] authParts = authString.split("\\s+");
			String authInfo = authParts[1];
			// Decode the data back to original string
			byte[] bytes = null;
		
			bytes = Base64.getDecoder().decode(authInfo);
			decodedAuth = new String(bytes);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error decoding base 64 HTTP authorization header information. User:password string must be properly base 64 encoded", e);
		}
		return decodedAuth;
	}
	
}
