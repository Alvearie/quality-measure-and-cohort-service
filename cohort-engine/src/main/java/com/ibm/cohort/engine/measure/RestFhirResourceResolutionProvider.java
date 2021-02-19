/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;

import com.ibm.cohort.version.SemanticVersion;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;

public abstract class RestFhirResourceResolutionProvider {

	protected MetadataResource queryWithVersion(IQuery<IBaseBundle> query, TokenClientParam versionParam, String version) {
		MetadataResource result = null;
		
		if( isEmpty( version ) ) {
			Bundle b = query.returnBundle(Bundle.class).execute();
			Optional<MetadataResource> optional = resolveResourceFromList(b);
			if( optional.isPresent() ) {
				result = optional.get();
			}
		} else {
			query.and(versionParam.exactly().code(version));
	
			Bundle b = query.returnBundle(Bundle.class).execute();
			if (b.getEntry().size() == 1) {
				result = (MetadataResource) b.getEntryFirstRep().getResource();
			} else {
				throw new IllegalArgumentException(String.format(
						"Measure lookup for %s returned unexpected number of elements %d", query.toString(), b.getEntry().size()));
			}
		}
		return result;
	}

	protected Optional<MetadataResource> resolveResourceFromList(Bundle bundle) {
		int numberOfMaxVersionSeen = 0;
		SemanticVersion currentMax = null;
		MetadataResource retVal = null;
	
		for (Bundle.BundleEntryComponent b : bundle.getEntry()) {
			MetadataResource measure = (Measure) b.getResource();
			Optional<SemanticVersion> optional = SemanticVersion.create(measure.getVersion());
			if (optional.isPresent()) {
				SemanticVersion measureVersion = optional.get();
	
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
