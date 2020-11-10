/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class RestFhirMeasureResolutionProvider implements MeasureResolutionProvider<Measure> {
	private IGenericClient measureClient;

	public RestFhirMeasureResolutionProvider(IGenericClient measureClient) {
		this.measureClient = measureClient;
	}

	@Override
	public Measure resolveMeasureById(String resourceID) {
		return measureClient.read().resource(Measure.class).withId(resourceID).execute();
	}

	@Override
	public Measure resolveMeasureByCanonicalUrl(String url) {
		Bundle b = measureClient.search().forResource(Measure.class).where(Measure.URL.matches().value(url))
				.returnBundle(Bundle.class).execute();
		if (b.getEntry().size() == 1) {
			return (Measure) b.getEntryFirstRep().getResource();
		} else {
			throw new IllegalArgumentException(String.format(
					"Measure lookup for url %s returned unexpected number of elements %d", url, b.getEntry().size()));
		}
	}

	@Override
	public Measure resolveMeasureByName(String name, String version) {
		Bundle b = measureClient.search().forResource(Measure.class).where(Measure.NAME.matchesExactly().value(name))
				.and(Measure.VERSION.exactly().code(version)).returnBundle(Bundle.class).execute();
		if (b.getEntry().size() == 1) {
			return (Measure) b.getEntryFirstRep().getResource();
		} else {
			throw new IllegalArgumentException(
					String.format("Measure lookup for name %s|%s returned unexpected number of elements %d", name, version,
							b.getEntry().size()));
		}
	}
}
