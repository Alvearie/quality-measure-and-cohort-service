/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.engine.measure.RestFhirLibraryResolutionProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.RelatedArtifact;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;

/**
 * Implementation of the MultiFormatLibrarySourceProvider that uses FHIR R4
 * Library resources as its source. Library relatedArtifacts with depends-on
 * relationships are walked recursively and any supported MIME type is added
 * to the source collection.
 */
public class FhirLibraryLibrarySourceProvider extends MultiFormatLibrarySourceProvider {

	private RestFhirLibraryResolutionProvider libraryResolutionProvider;

	public FhirLibraryLibrarySourceProvider(IGenericClient fhirClient) {
		this.libraryResolutionProvider = new RestFhirLibraryResolutionProvider(fhirClient);
	}

	public FhirLibraryLibrarySourceProvider(IGenericClient fhirClient, String rootLibraryId) {
		this(fhirClient);
		org.hl7.fhir.r4.model.Library rootLibrary = fhirClient.read().resource(org.hl7.fhir.r4.model.Library.class)
				.withId(rootLibraryId).execute();
		loadRecursively(rootLibrary);
	}

	public FhirLibraryLibrarySourceProvider(IGenericClient fhirClient, org.hl7.fhir.r4.model.Library rootLibrary) {
		this(fhirClient);
		loadRecursively(rootLibrary);
	}

	public int loadRecursively(org.hl7.fhir.r4.model.Library library) {
		int numLoaded = 0;
		if (library.hasContent()) {
			for (Attachment attachment : library.getContent()) {
				if (LibraryFormat.isSupportedMimeType(attachment.getContentType())) {
					String libraryId = library.getName();
					String version = library.getVersion();

					VersionedIdentifier id = new VersionedIdentifier().withId(libraryId);
					if (version != null) {
						id.setVersion(version);
					}

					LibraryFormat sourceFormat = LibraryFormat.forMimeType(attachment.getContentType());

					Map<LibraryFormat, InputStream> formats = sources.computeIfAbsent(id,
							vid -> new HashMap<LibraryFormat, InputStream>());

					formats.put(sourceFormat, new ByteArrayInputStream(attachment.getData()));
					numLoaded++;
				}
			}
		}

		// TODO - Add cycle and duplicate detection
		if (library.hasRelatedArtifact()) {
			for (RelatedArtifact related : library.getRelatedArtifact()) {
				if (related.getType() == RelatedArtifactType.DEPENDSON && related.getResource().contains("Library/")) {
					org.hl7.fhir.r4.model.Library relatedLibrary = libraryResolutionProvider.resolveLibraryByCanonicalUrl(related.getResource());
					numLoaded += loadRecursively(relatedLibrary);
				}
			}
		}

		return numLoaded;
	}
}
