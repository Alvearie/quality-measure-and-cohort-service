package com.ibm.cohort.engine;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;

public class FhirClientFactory {
	
	public static FhirClientFactory newInstance(FhirContext fhirContext) {
		return new FhirClientFactory(fhirContext);
	}

	private FhirContext fhirContext;
	
	protected FhirClientFactory(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}
	
	/**
	 * Create a HAPI FHIR client with appropriate configuration settings for the
	 * provided FhirServerConfig
	 * 
	 * @param config      FhirServerConfig containing configuration details for the
	 *                    HAPI client
	 * @return configured HAPI FHIR client
	 */
	public IGenericClient createFhirClient(FhirServerConfig config) {
		
		IGenericClient client = fhirContext.newRestfulGenericClient(config.getEndpoint());

		if (config.getUser() != null && config.getPassword() != null) {
			/**
			 * @see https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section2
			 */
			IClientInterceptor authInterceptor = new BasicAuthInterceptor(config.getUser(), config.getPassword());
			client.registerInterceptor(authInterceptor);
		}

		Map<String, String> additionalHeaders = config.getAdditionalHeaders();
		if (additionalHeaders != null && additionalHeaders.size() > 0) {
			/**
			 * @see https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section4
			 */
			AdditionalRequestHeadersInterceptor tenantInterceptor = new AdditionalRequestHeadersInterceptor();
			for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
				tenantInterceptor.addHeaderValue(entry.getKey(), entry.getValue());
			}
			client.registerInterceptor(tenantInterceptor);
		}

		return client;
	}
}
