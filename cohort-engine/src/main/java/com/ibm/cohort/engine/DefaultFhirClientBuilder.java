/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.CookieInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

/**
 * Default implementation of a FhirClientBuilder that configures the HAPI client
 * with most of the out-of-the-box client interceptors detailed in <a href=
 * "https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html">the
 * HAPI documentation</a>.
 * 
 * The builder does not attempt to validate the FhirServerConfig object. For
 * instance, if the client provides both user/password authentication parameters
 * and a bearer authorization token, both interceptors are created and attached
 * to the client object.
 */
public class DefaultFhirClientBuilder implements FhirClientBuilder {

	private FhirContext fhirContext;

	public DefaultFhirClientBuilder(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}

	/**
	 * Create a HAPI FHIR client with appropriate configuration settings for the
	 * provided FhirServerConfig
	 * 
	 * @param config FhirServerConfig containing configuration details for the HAPI
	 *               client
	 * @return configured HAPI FHIR client
	 */
	@Override
	public IGenericClient createFhirClient(FhirServerConfig config) {

		IGenericClient client = fhirContext.newRestfulGenericClient(config.getEndpoint());

		if (config.getLogInfo() != null) {
			/**
			 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section1
			 */
			LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
			for (FhirServerConfig.LogInfo logInfo : config.getLogInfo()) {
				switch (logInfo) {
				case REQUEST_BODY:
					loggingInterceptor.setLogRequestBody(true);
					break;
				case REQUEST_HEADERS:
					loggingInterceptor.setLogRequestHeaders(true);
					break;
				case REQUEST_SUMMARY:
					loggingInterceptor.setLogRequestSummary(true);
					break;
				case RESPONSE_BODY:
					loggingInterceptor.setLogResponseBody(true);
					break;
				case RESPONSE_HEADERS:
					loggingInterceptor.setLogResponseHeaders(true);
					break;
				case RESPONSE_SUMMARY:
					loggingInterceptor.setLogResponseSummary(true);
					break;
				case ALL:
					loggingInterceptor.setLogRequestBody(true);
					loggingInterceptor.setLogRequestHeaders(true);
					loggingInterceptor.setLogRequestSummary(true);
					loggingInterceptor.setLogResponseBody(true);
					loggingInterceptor.setLogResponseHeaders(true);
					loggingInterceptor.setLogResponseSummary(true);
					break;
				}
			}
			client.registerInterceptor(loggingInterceptor);
		}

		if (config.getUser() != null && config.getPassword() != null) {
			/**
			 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section2
			 */
			IClientInterceptor authInterceptor = new BasicAuthInterceptor(config.getUser(), config.getPassword());
			client.registerInterceptor(authInterceptor);
		}

		if (config.getToken() != null) {
			/**
			 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section3
			 */
			IClientInterceptor authInterceptor = new BearerTokenAuthInterceptor(config.getToken());
			client.registerInterceptor(authInterceptor);
		}

		/**
		 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section4
		 */
		if (config.getHeaders() != null) {
			AdditionalRequestHeadersInterceptor tenantInterceptor = new AdditionalRequestHeadersInterceptor();
			for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
				tenantInterceptor.addHeaderValue(entry.getKey(), entry.getValue());
			}
			client.registerInterceptor(tenantInterceptor);
		}

		Map<String, String> additionalHeaders = config.getAdditionalHeaders();
		if (additionalHeaders != null && additionalHeaders.size() > 0) {
			/**
			 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section4
			 */
			AdditionalRequestHeadersInterceptor tenantInterceptor = new AdditionalRequestHeadersInterceptor();
			for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
				tenantInterceptor.addHeaderValue(entry.getKey(), entry.getValue());
			}
			client.registerInterceptor(tenantInterceptor);
		}

		if (config.getCookies() != null) {
			/**
			 * https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section5
			 */
			for (String cookie : config.getCookies()) {
				IClientInterceptor cookieInterceptor = new CookieInterceptor(cookie);
				client.registerInterceptor(cookieInterceptor);
			}
		}

		return client;
	}
}
