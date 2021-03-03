/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import com.ibm.cohort.engine.helpers.CanonicalHelper;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/**
 * Provide a very basic mechanism for retrieving FHIR Library resources from a
 * FHIR server using FHIR REST APIs.
 * 
 * TODO - Consider loading Library and all related dependencies
 *       (relatedArtifacts[type="depends-on"]) in a single call and caching the
 *       results. A FHIR search call would look something like
 *       "/Library?identifier=SampleLibrary&amp;_include=Library:depends-on". The
 *       FHIR server does not support _include:iterate tree walking today, so
 *       that needs to be considered if we go down this route.
 */
public class RestFhirLibraryResolutionProvider extends RestFhirResourceResolutionProvider implements LibraryResolutionProvider<Library> {

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
			return resolveLibraryByIdRaw(libraryId);
		});
		if( library != null ) {
			cacheByUrl.computeIfAbsent(getUrlCacheKey(library), key -> library);
			cacheByNameVersion.computeIfAbsent(getCacheKey(library), key -> library);
		}
		return library;
	}
	
	public Library resolveLibraryByIdRaw(String libraryId) {
		Library result = null;
		try {
			result = libraryClient.read().resource(Library.class).withId(libraryId).execute();
		} catch( ResourceNotFoundException rnfe ) {
			result = null;
		}
		return result;
	}

	@Override
	public Library resolveLibraryByName(String libraryName, String libraryVersion) {
		String cacheKey = getCacheKey(libraryName, libraryVersion);
		Library library = cacheByNameVersion.computeIfAbsent(cacheKey, k -> {
			return resolveLibraryByNameRaw(libraryName, libraryVersion);
		});
		if( library != null ) {
			cacheById.computeIfAbsent(library.getId(), key -> library);
			cacheByUrl.computeIfAbsent(getUrlCacheKey(library), key -> library);
		}
		return library;
	}
	
	public Library resolveLibraryByNameRaw(String libraryName, String libraryVersion) {
		IQuery<IBaseBundle> query = libraryClient.search().forResource(Library.class)
					.where(Library.NAME.matches().value(libraryName));
		return (Library) queryWithVersion( query, Library.VERSION, libraryVersion);
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
		Library library = cacheByUrl.computeIfAbsent(libraryUrl, cacheKey -> {
			return resolveLibraryByCanonicalUrlRaw(libraryUrl);
		});
		if( library != null ) {
			cacheById.computeIfAbsent(library.getId(), key -> library);
			cacheByNameVersion.computeIfAbsent(getCacheKey(library), key -> library);
		}
		return library;
	}
	
	public Library resolveLibraryByCanonicalUrlRaw(String libraryUrl) {
		// The IBM FHIR Server does not honor the vertical bar convention yet.
		Pair<String,String> parts = CanonicalHelper.separateParts(libraryUrl);

		IQuery<IBaseBundle> query = libraryClient.search().forResource(Library.class)
				.where(Library.URL.matches().value(parts.getLeft()));
		return (Library) queryWithVersion(query, Library.VERSION, parts.getRight());
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("No support for Library updates");
	}
	
	public void clearCache() {
		cacheById.clear();
		cacheByNameVersion.clear();
		cacheByUrl.clear();
	}
	
	protected String getUrlCacheKey(Library library) {
		return CanonicalHelper.toCanonicalUrl(library);
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
