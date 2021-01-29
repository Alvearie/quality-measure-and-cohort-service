/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.fhir.client.config;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.StringUtils;

import ca.uhn.fhir.context.FhirContext;

/**
 * Defines a factory API that enables applications to obtain a builder that
 * produces HAPI FHIR Client objects based on externalized configuration data.
 */
public abstract class FhirClientBuilderFactory {

	public static final String IMPL_CLASS_NAME = "com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory";
	public static final String DEFAULT_IMPL_CLASS_NAME = "com.ibm.cohort.fhir.client.config.DefaultFhirClientBuilderFactory";

	protected FhirClientBuilderFactory() {

	}

	/**
	 * Obtain a new instance of a FhirClientBuilderFactory. This static method
	 * creates a new factory instance. The method uses the following ordered lookup
	 * procedure to determine the FhirClientBuilderFactory implementation class to
	 * load:
	 * 
	 * <ul>
	 * <li>Use the com.ibm.cohort.FhirClientBuilderFactory</li> system property.
	 * <li>Default FhirClientBuilderFactory instance.
	 * </ul>
	 * 
	 * @return New instance of a FhirClientBuilderFactory
	 */
	public static FhirClientBuilderFactory newInstance() {
		String implName = System.getProperty(IMPL_CLASS_NAME);
		if (StringUtils.isEmpty(implName)) {
			implName = DEFAULT_IMPL_CLASS_NAME;
		}

		try {
			Class<? extends FhirClientBuilderFactory> clazz = Class.forName(implName)
					.asSubclass(FhirClientBuilderFactory.class);
			Constructor<? extends FhirClientBuilderFactory> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch (Exception ex) {
			throw new FactoryConfigurationError(ex);
		}
	}

	/**
	 * Return a new FHIR Client Builder that uses the platform default FHIR Context.
	 * 
	 * @return New FhirClientBuilder instance.
	 */
	public abstract FhirClientBuilder newFhirClientBuilder();

	/**
	 * Return a new FHIR Client Builder that uses the provided FHIR Context.
	 * 
	 * @param fhirContext HAPI FhirContext object configured for the target FHIR
	 *                    server/version.
	 * 
	 * @return New FhirClientBuilder instance.
	 */
	public abstract FhirClientBuilder newFhirClientBuilder(FhirContext fhirContext);
}
