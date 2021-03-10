/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class FHIRClientContext {

	public static class Builder {

		private FhirContext fhirContext;

		private IGenericClient defaultClient;
		public Builder withDefaultClient(IGenericClient value) {
			this.defaultClient = value;
			return this;
		}
		public Builder withDefaultClient(FhirServerConfig value) {
			return withDefaultClient(createClient(value));
		}

		private IGenericClient terminologyClient;
		public Builder withTerminologyClient(IGenericClient value) {
			this.terminologyClient = value;
			return this;
		}
		public Builder withTerminologyClient(FhirServerConfig value) {
			return withTerminologyClient(createClient(value));
		}

		private IGenericClient dataClient;
		public Builder withDataClient(IGenericClient value) {
			this.dataClient = value;
			return this;
		}
		public Builder withDataClient(FhirServerConfig value) {
			return withDataClient(createClient(value));
		}

		private IGenericClient measureClient;
		public Builder withMeasureClient(IGenericClient value) {
			this.measureClient = value;
			return this;
		}
		public Builder withMeasureClient(FhirServerConfig value) {
			return withMeasureClient(createClient(value));
		}

		private IGenericClient libraryClient;
		public Builder withLibraryClient(IGenericClient value) {
			this.libraryClient = value;
			return this;
		}
		public Builder withLibraryClient(FhirServerConfig value) {
			return withLibraryClient(createClient(value));
		}

		public FHIRClientContext build() {
			return new FHIRClientContext(
					getClientOrDefault(terminologyClient),
					getClientOrDefault(dataClient),
					getClientOrDefault(measureClient),
					getClientOrDefault(libraryClient)
			);
		}

		private IGenericClient getClientOrDefault(IGenericClient client) {
			IGenericClient retVal = client == null ? defaultClient : client;

			if (retVal == null) {
				// TODO: Throw a real error or something.
				throw new RuntimeException("No valid client set");
			}

			return retVal;
		}

		private IGenericClient createClient(FhirServerConfig config) {
			FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();
			FhirClientBuilder clientBuilder = factory.newFhirClientBuilder(getFhirContext());
			return clientBuilder.createFhirClient(config);
		}

		private FhirContext getFhirContext() {
			if (fhirContext == null) {
				fhirContext = FhirContext.forR4();
			}
			return fhirContext;
		}
	}

	private final IGenericClient terminologyClient;
	private final IGenericClient dataClient;
	private final IGenericClient measureClient;
	private final IGenericClient libraryClient;

	private FHIRClientContext(
			IGenericClient terminologyClient,
			IGenericClient dataClient,
			IGenericClient measureClient,
			IGenericClient libraryClient
	) {
		this.terminologyClient = terminologyClient;
		this.dataClient = dataClient;
		this.measureClient = measureClient;
		this.libraryClient = libraryClient;
	}

	public IGenericClient getTerminologyClient() {
		return terminologyClient;
	}

	public IGenericClient getDataClient() {
		return dataClient;
	}

	public IGenericClient getMeasureClient() {
		return measureClient;
	}

	public IGenericClient getLibraryClient() {
		return libraryClient;
	}

}
