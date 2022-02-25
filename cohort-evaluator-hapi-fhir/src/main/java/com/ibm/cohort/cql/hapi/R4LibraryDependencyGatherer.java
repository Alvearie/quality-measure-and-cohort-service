/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.helpers.CanonicalHelper;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.codesystems.LibraryType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gathers all logic {@link Library} dependencies for a specified
 * {@link Library} id or all logic libraries referenced by a {@link Measure}.
 */
public class R4LibraryDependencyGatherer {

    private final FhirResourceResolver<Library> libraryResolver;

    public R4LibraryDependencyGatherer(FhirResourceResolver<Library> libraryResolver) {
        this.libraryResolver = libraryResolver;
    }

    public List<Library> gatherForLibraryId(String rootLibraryId) {
        List<Library> retVal = Collections.emptyList();
        Library rootLibrary = resolveLibrary(rootLibraryId);
        if (rootLibrary != null) {
            retVal = recurse(rootLibrary, new HashSet<>());
        }
        return retVal;
    }

    public List<Library> gatherForMeasure(Measure measure) {
        List<Library> retVal = new ArrayList<>();
        Set<String> loadedCanonicalUrls = new HashSet<>();
        for (CanonicalType ref : measure.getLibrary()) {
            Library library = resolveLibrary(ref.getValue());
            if (library != null) {
                retVal.addAll(recurse(library, loadedCanonicalUrls));
            }
        }
        return retVal;
    }

    private List<Library> recurse(Library library, Set<String> loadedCanonicalUrls) {
        String canonicalUrl = CanonicalHelper.toCanonicalUrl(library.getUrl(), library.getVersion());
        if (loadedCanonicalUrls.contains(canonicalUrl)) {
            return Collections.emptyList();
        }

        List<Library> retVal = new ArrayList<>();
        retVal.add(library);
        loadedCanonicalUrls.add(canonicalUrl);

        if (library.hasRelatedArtifact()) {
            for (RelatedArtifact related : library.getRelatedArtifact()) {
                if (related.hasType() && related.getType().equals(RelatedArtifact.RelatedArtifactType.DEPENDSON) && related.hasResource()) {
                    String relatedIdentifier = related.getResource();
                    Library relatedLibrary = resolveLibrary(relatedIdentifier);
                    if (relatedLibrary != null) {
                        retVal.addAll(recurse(relatedLibrary, loadedCanonicalUrls));
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * Resolve a FHIR Library resource based on the string reference provided.
     *
     * @param resource                String reference to a FHIR Library resource
     * @return Loaded FHIR Library resource or null if the String does not contain
     *         an understood reference
     */
    private Library resolveLibrary(String resource) {

        Library library = null;

        // Raw references to Library/libraryId or libraryId
        if (resource.startsWith("Library/") || !resource.contains("/")) {
            library = libraryResolver.resolveById(resource.replace("Library/", ""));
        }
        // Full url (e.g. http://hl7.org/fhir/us/Library/FHIRHelpers)
        else if (resource.contains(("/Library/"))) {
            library = libraryResolver.resolveByCanonicalUrl(resource);
        }

        if( isLogicLibrary(library) ) {
            return library;
        } else {
            return null;
        }
    }

    /**
     * Perform basic checks to verify that the loaded Library contains CQL/ELM logic that
     * can be evaluated by the runtime engine. This logic is cribbed from the cqf-ruler
     * LibraryHelper implementation. It is specificaly useful for Library content related
     * to the measure, such as the FHIR modelinfo file, that is not directly part of the
     * CQL logic.
     *
     * @param library FHIR Library resource
     * @return true if the Library resource contains executable CQL logic or false otherwise
     */
    private static boolean isLogicLibrary(Library library) {
        if (library == null) {
            return false;
        }

        if (!library.hasType()) {
            // If no type is specified, assume it is a logic library based on whether there is a CQL content element.
            if (library.hasContent()) {
                for (Attachment a : library.getContent()) {
                    if (a.hasContentType() && (a.getContentType().equals("text/cql")
                            || a.getContentType().equals("application/elm+xml")
                            || a.getContentType().equals("application/elm+json"))) {
                        return true;
                    }
                }
            }
            return false;
        }

        if (!library.getType().hasCoding()) {
            return false;
        }

        for (Coding c : library.getType().getCoding()) {
            if (c.hasSystem() && c.getSystem().equals(LibraryType.LOGICLIBRARY.getSystem())
                    && c.hasCode() && c.getCode().equals(LibraryType.LOGICLIBRARY.toCode())) {
                return true;
            }
        }

        return false;
    }
}
