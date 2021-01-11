/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Optional;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
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

	@Override
	public Measure resolveMeasureByIdentifier(Identifier identifier, String version) {
		if (isEmpty(version)) {
			return resolveMeasureByIdentifierOnly(identifier);
		} else {
			return resolveMeasureByIdentifierWithVersion(identifier, version);
		}
	}
	
	private Measure resolveMeasureByIdentifierOnly(Identifier identifier) {
		Bundle b = measureClient.search().forResource(Measure.class)
				.where(Measure.IDENTIFIER.exactly().systemAndValues(identifier.getSystem(), identifier.getValue()))
				.returnBundle(Bundle.class).execute();
		if (b.getEntry().isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Measure lookup for identifier: %s returned an unexpected number of results", identifier));
		} else {
			Optional<Measure> optional = resolveMeasureFromList(b);
			if (!optional.isPresent()) {
				throw new IllegalArgumentException(
						String.format("Measure lookup for identifier: %s did not yield a definitive result with a semantic version", identifier));
			}
			return optional.get();
		}
	}
	
	private Measure resolveMeasureByIdentifierWithVersion(Identifier identifier, String version) {
		Bundle b = measureClient.search().forResource(Measure.class)
				.where(Measure.IDENTIFIER.exactly().systemAndValues(identifier.getSystem(), identifier.getValue()))
				.and(Measure.VERSION.exactly().code(version))
				.returnBundle(Bundle.class).execute();
		if (b.getEntry().size() == 1) {
			return (Measure) b.getEntryFirstRep().getResource();
		} else {
			throw new IllegalArgumentException(
					String.format("Measure lookup for identifier: %s version: %s returned unexpected number of results", identifier, version));
		}
	}
	
	private Optional<Measure> resolveMeasureFromList(Bundle bundle) {
		int numberOfMaxVersionSeen = 0;
		MeasureVersion currentMax = null;
		Measure retVal = null;

		for (Bundle.BundleEntryComponent b : bundle.getEntry()) {
			Measure measure = (Measure) b.getResource();
			Optional<MeasureVersion> optional = MeasureVersion.create(measure.getVersion());
			if (optional.isPresent()) {
				MeasureVersion measureVersion = optional.get();

				// Max not initialized or new max found
				if (currentMax == null || measureVersion.compareTo(currentMax) > 0) {
					retVal = measure;
					currentMax = measureVersion;
					numberOfMaxVersionSeen = 1;
				} else if (measureVersion.equals(currentMax)) {
					numberOfMaxVersionSeen += 1;
				}
			}
		}

		if (numberOfMaxVersionSeen != 1) {
			return Optional.empty();
		} else {
			return Optional.of(retVal);
		}
	}
}
