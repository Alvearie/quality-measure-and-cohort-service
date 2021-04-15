/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * A wrapper around the different HAPI client connections required by the engine.
 *
 * FHIRClientContext objects are expensive to create. If possible, a single
 * FHIRClientContext object should be created and then reused as needed.
 *
 * Use {@link Builder} for construction.
 */
public class FHIRClientContext {

	/**
	 * <p>Builds instances of {@link FHIRClientContext}.
	 *
	 * <p>Allows up to four unique connections that access the following services:
	 * <ul>
	 *     <li>Terminology
	 *     <li>Data
	 *     <li>Measure
	 *     <li>Library
	 * </ul>
	 *
	 * <p>A default connection may also be provided in place of any of the above mentioned connections.
	 * If any connection is not provided while there is a default, the default connection is used.
	 *
	 * <p>Failing to provide a valid connection for all four services will result in an {@link IllegalArgumentException}.
	 */
	public static class Builder {

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
				throw new IllegalArgumentException("No valid client or configuration found");
			}

			return retVal;
		}

		private IGenericClient createClient(FhirServerConfig config) {
			FhirClientBuilderFactory factory = new DefaultFhirClientBuilderFactory();
			FhirClientBuilder clientBuilder = factory.newFhirClientBuilder();
			return clientBuilder.createFhirClient(config);
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
