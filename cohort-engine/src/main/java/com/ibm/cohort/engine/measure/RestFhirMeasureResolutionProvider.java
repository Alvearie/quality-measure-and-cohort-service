/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;

import com.ibm.cohort.engine.helpers.CanonicalHelper;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class RestFhirMeasureResolutionProvider extends RestFhirResourceResolutionProvider implements MeasureResolutionProvider<Measure> {
	private IGenericClient measureClient;

	public RestFhirMeasureResolutionProvider(IGenericClient measureClient) {
		this.measureClient = measureClient;
	}

	@Override
	public Measure resolveMeasureById(String resourceID) {
		Measure result = null;
		try {
			result = measureClient.read().resource(Measure.class).withId(resourceID).execute();
		} catch( ResourceNotFoundException rnfe ) {
			result = null;
		}
		return result;
	}

	@Override
	public Measure resolveMeasureByCanonicalUrl(String url) {
		Pair<String,String> parts = CanonicalHelper.separateParts(url);
		
		IQuery<IBaseBundle> query = measureClient.search().forResource(Measure.class).where(Measure.URL.matches().value(parts.getLeft()));
		return (Measure) queryWithVersion(query, Measure.VERSION, parts.getRight());
	}

	@Override
	public Measure resolveMeasureByName(String name, String version) {
		IQuery<IBaseBundle> query = measureClient.search().forResource(Measure.class).where(Measure.NAME.matchesExactly().value(name));
		return (Measure) queryWithVersion(query, Measure.VERSION, version);
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
		Measure result = null;
		
		Bundle b = measureClient.search().forResource(Measure.class)
				.where(Measure.IDENTIFIER.exactly().systemAndValues(identifier.getSystem(), identifier.getValue()))
				.returnBundle(Bundle.class).execute();
		if (b.hasEntry()) {
			Optional<MetadataResource> optional = resolveResourceFromList(b);
			if (optional.isPresent()) {
				result = (Measure) optional.get(); 
			}
		}
		
		return result;
	}
	
	private Measure resolveMeasureByIdentifierWithVersion(Identifier identifier, String version) {
		Measure result = null;
		
		Bundle b = measureClient.search().forResource(Measure.class)
				.where(Measure.IDENTIFIER.exactly().systemAndValues(identifier.getSystem(), identifier.getValue()))
				.and(Measure.VERSION.exactly().code(version))
				.returnBundle(Bundle.class).execute();
		if( b.hasEntry() ) { 
			if (b.getEntry().size() == 1) {
				result = (Measure) b.getEntryFirstRep().getResource();
			} else {
				throw new IllegalArgumentException(
						String.format("Measure lookup for identifier: %s version: %s returned unexpected number of results: %s", identifier.getValue(), version, b.getEntry().size()));
			}
		}
		
		return result;
	}
}
