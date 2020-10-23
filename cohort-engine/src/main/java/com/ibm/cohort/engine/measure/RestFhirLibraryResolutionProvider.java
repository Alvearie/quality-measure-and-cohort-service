package com.ibm.cohort.engine.measure;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Provide a very basic mechanism for retrieving FHIR Library resources from a
 * FHIR server using FHIR REST APIs.
 * 
 * @todo - Add Library caching
 * @todo - Consider loading Library and all related dependencies
 *       (relatedArtifacts[type="depends-on"]) in a single call and caching the
 *       results. A FHIR search call would look something like
 *       "/Library?identifier=SampleLibrary&_include=Library:depends-on". The FHIR server
 *       does not support _include:iterate tree walking today, so that needs to be considered
 *       if we go down this route.
 */
public class RestFhirLibraryResolutionProvider implements LibraryResolutionProvider<Library> {

	private IGenericClient libraryClient;

	public RestFhirLibraryResolutionProvider(IGenericClient libraryClient) {
		this.libraryClient = libraryClient;
	}

	@Override
	public Library resolveLibraryById(String libraryId) {
		return libraryClient.read().resource(Library.class).withId(libraryId).execute();
	}

	@Override
	public Library resolveLibraryByName(String libraryName, String libraryVersion) {
		try {
			Bundle bundle = libraryClient.search().forResource(Library.class)
					.where(Library.NAME.matches().value(libraryName))
					.and(Library.VERSION.exactly().code(libraryVersion)).returnBundle(Bundle.class).sort()
					.descending(Library.DATE).execute();

			if (bundle.getTotal() == 0) {
				throw new IllegalArgumentException(
						String.format("No library found matching %s version %s", libraryName, libraryVersion));
			}

			return (Library) bundle.getEntry().get(0).getResource();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
		Bundle bundle = libraryClient.search().forResource(Library.class).where(Library.URL.matches().value(libraryUrl))
				.returnBundle(Bundle.class).sort().descending(Library.DATE).execute();

		if (bundle.getTotal() == 0) {
			throw new IllegalArgumentException(String.format("No library found matching url %s ", libraryUrl));
		}

		return (Library) bundle.getEntry().get(0).getResource();
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("No support for Library updates");
	}

}
