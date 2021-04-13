/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.fhir.client.config;

import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
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
 * 
 * Note that a FhirContext object used to create a DefaultFhirClientBuilder contains
 * static state that will be shared across any IGenericClient objects created from
 * the context. Reusing FhirContext objects across multiple instances of DefaultFhirClientBuilder
 * or creating multiple IGenericClient objects from a single DefaultFhirClientBuilder may
 * result in client configuration states that are unexpected.
 * 
 * It is recommended to create only a single IGenericClient from each instance of
 * DefaultFhirClientBuilder and to always provide a new FhirContext object to each
 * separate instance of DefaultFhirClientBuilder created in order to avoid potential
 * configuration issues.
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

		fhirContext.getRestfulClientFactory().setSocketTimeout(validateAndGetTimeoutConfig(config.getSocketTimeout(), IRestfulClientFactory.DEFAULT_SOCKET_TIMEOUT));
		fhirContext.getRestfulClientFactory().setConnectTimeout(validateAndGetTimeoutConfig(config.getConnectTimeout(), IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT));
		fhirContext.getRestfulClientFactory().setConnectionRequestTimeout(validateAndGetTimeoutConfig(config.getConnectionRequestTimeout(), IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT));

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

	private Integer validateAndGetTimeoutConfig(Integer configValue, Integer defaultValue) {
		if (configValue == null) {
			return defaultValue;
		} else if (configValue >= 0) {
			return configValue;
		} else {
			throw new IllegalArgumentException("FHIR client config timeout values must be >= 0");
		}
	}
}
