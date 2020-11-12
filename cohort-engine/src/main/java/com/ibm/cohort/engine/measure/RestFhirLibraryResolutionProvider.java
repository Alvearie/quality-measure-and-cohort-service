/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Map;
import java.util.WeakHashMap;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;

/**
 * Provide a very basic mechanism for retrieving FHIR Library resources from a
 * FHIR server using FHIR REST APIs.
 * 
 * @todo - Consider loading Library and all related dependencies
 *       (relatedArtifacts[type="depends-on"]) in a single call and caching the
 *       results. A FHIR search call would look something like
 *       "/Library?identifier=SampleLibrary&_include=Library:depends-on". The
 *       FHIR server does not support _include:iterate tree walking today, so
 *       that needs to be considered if we go down this route.
 */
public class RestFhirLibraryResolutionProvider implements LibraryResolutionProvider<Library> {

	private Map<String, Library> cacheByNameVersion = new WeakHashMap<>();
	private Map<String, Library> cacheById = new WeakHashMap<>();
	private Map<String, Library> cacheByUrl = new WeakHashMap<>();

	private IGenericClient libraryClient;

	public RestFhirLibraryResolutionProvider(IGenericClient libraryClient) {
		this.libraryClient = libraryClient;
	}

	@Override
	public Library resolveLibraryById(String libraryId) {
		Library library = cacheById.computeIfAbsent(libraryId, k -> {
			return libraryClient.read().resource(Library.class).withId(libraryId).execute();
		});
		cacheByNameVersion.computeIfAbsent(getCacheKey(library), k -> library);
		return library;
	}

	@Override
	public Library resolveLibraryByName(String libraryName, String libraryVersion) {
		String cacheKey = getCacheKey(libraryName, libraryVersion);
		Library library = cacheByNameVersion.computeIfAbsent(cacheKey, k -> {
			Bundle bundle = null;
			try {
				bundle = libraryClient.search().forResource(Library.class)
						.where(Library.NAME.matches().value(libraryName))
						.and(Library.VERSION.exactly().code(libraryVersion)).returnBundle(Bundle.class).sort()
						.descending(Library.DATE).execute();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

			if (bundle.getTotal() == 0) {
				throw new IllegalArgumentException(
						String.format("No library found matching %s version %s", libraryName, libraryVersion));
			}

			return (Library) bundle.getEntry().get(0).getResource();

		});
		return library;
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
				
		return cacheByUrl.computeIfAbsent(libraryUrl, cacheKey -> {
			// The IBM FHIR Server does not honor the vertical bar convention yet.
			String [] parts = libraryUrl.split("\\|");

			IQuery<Bundle> query = libraryClient.search().forResource(Library.class)
					.where(Library.URL.matches().value(parts[0])).returnBundle(Bundle.class);
			if( parts.length > 1 ) {
				query = query.and(Library.VERSION.exactly().identifier(parts[1]));
			}
			
			Bundle bundle = query.returnBundle(Bundle.class).execute();
			if (bundle.getTotal() != 1) {
				throw new IllegalArgumentException(String.format("Unexpected number of libraries %d matching url %s ",
						bundle.getTotal(), query.toString()));
			}

			return (Library) bundle.getEntry().get(0).getResource();
		});
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("No support for Library updates");
	}

	protected String getCacheKey(Library library) {
		return getCacheKey(library.getName(), library.getVersion());
	}

	protected String getCacheKey(String name, String version) {
		String key = name;
		if (version != null) {
			key = key + "-" + version;
		}
		return key;
	}
}
