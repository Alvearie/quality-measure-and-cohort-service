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

import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.RelatedArtifact;

import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Implementation of the MultiFormatLibrarySourceProvider that uses FHIR R4
 * Library resources as its source. Library relatedArtifacts with depends-on
 * relationships are walked recursively and any supported MIME type is added
 * to the source collection.
 */
public class FhirLibraryLibrarySourceProvider extends MultiFormatLibrarySourceProvider {

	private IGenericClient fhirClient;

	public FhirLibraryLibrarySourceProvider(IGenericClient fhirClient) {
		this.fhirClient = fhirClient;
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
				// TODO: Beef this logic up to handle situations where this logic uses a full
				// canonical URL
				if (related.getResource().matches("(^|/)Library.*")) {
					org.hl7.fhir.r4.model.Library relatedLibrary = fhirClient.read()
							.resource(org.hl7.fhir.r4.model.Library.class).withUrl(related.getResource()).execute();
					numLoaded += loadRecursively(relatedLibrary);
				}
			}
		}

		return numLoaded;
	}
}
