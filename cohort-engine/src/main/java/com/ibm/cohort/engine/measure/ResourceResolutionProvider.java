/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import com.ibm.cohort.engine.helpers.CanonicalHelper;
import com.ibm.cohort.version.SemanticVersion;

import ca.uhn.fhir.parser.IParser;

/**
 * Implementation of common logic for FHIR knowledge artifact resource
 * resolution. This serves as the foundation for several specific resource
 * resolution mechanisms (e.g. directory, zip, etc.). Resources that are
 * processed by this implementation are cached in an instance-level cache that
 * may not be suitable for environments where the knowledge associated with
 * these artifacts might change without revving the unique identifiers.
 */
public abstract class ResourceResolutionProvider
		implements LibraryResolutionProvider<Library>, MeasureResolutionProvider<Measure> {
	private Map<String, Map<String, MetadataResource>> resourcesByIdByResourceType = new HashMap<>();
	private Map<String, Map<String, SortedMap<SemanticVersion, MetadataResource>>> resourcesByNameByResourceType = new HashMap<>();
	private Map<String, Map<String, SortedMap<SemanticVersion, MetadataResource>>> resourcesByUrlByResourceType = new HashMap<>();
	
	/**
	 * Process a FHIR resource. This parses the resource, resolves the unique
	 * identifiers, and adds the resource to the underlying query caches.
	 * 
	 * @param resourceName Artifact name (e.g. filename, zip entry name, etc.) of
	 *                     the resource that is being loaded. In circumstances where
	 *                     no Resource.id is provided, this will serve as a
	 *                     stand-in.
	 * @param resourceData Stream containing the input data to be processed.
	 * @param parser       HAPI FHIR resource parser.
	 * @throws IOException when resource data cannot be read from the stream.
	 */
	protected void processResource(String resourceName, InputStream resourceData, IParser parser) throws IOException {
		IBaseResource resource = parser.parseResource(resourceData);
		processResource( resourceName, resource);
	}
	
	/**
	 * Process a FHIR resource. This parses the resource, resolves the unique
	 * identifiers, and adds the resource to the underlying query caches.
	 * 
	 * @param resourceName Artifact name (e.g. filename, zip entry name, etc.) of
	 *                     the resource that is being loaded. In circumstances where
	 *                     no Resource.id is provided, this will serve as a
	 *                     stand-in.
	 * @param resource	   deserialized FHIR resource object
	 * @throws IOException when resource data cannot be read from the stream.
	 */
	protected void processResource(String resourceName, IBaseResource resource) throws IOException {

		if (resource instanceof MetadataResource) {
			MetadataResource metadataResource = (MetadataResource) resource;

			if (metadataResource.getId() == null) {
				metadataResource.setId(FilenameUtils.getBaseName(resourceName));
			}

			addToIndexes(metadataResource);
		} else if (resource instanceof Bundle) {
			Bundle bundle = (Bundle) resource;

			for (BundleEntryComponent be : bundle.getEntry()) {
				if (be.getResource() instanceof MetadataResource) {
					MetadataResource metadataResource = (MetadataResource) be.getResource();
					if (metadataResource.getId() == null) {
						throw new IllegalArgumentException("Bundle resources must contain an ID value");
					}

					addToIndexes(metadataResource);
				}
			}
		} else {
			throw new UnsupportedOperationException("Only FHIR knowledge artifacts as single resources or in a bundle are supported.");
		}
	}

	/**
	 * Add a knowledge artifact to the indexes based on unique business identifiers.
	 * 
	 * @param resource knowledge artifact
	 */
	protected void addToIndexes(MetadataResource resource) {

		if (resource.getId() == null) {
			throw new IllegalArgumentException("Knowledge artifacts must contain an id");
		}

		Map<String, MetadataResource> typedResourcesById = resourcesByIdByResourceType
				.computeIfAbsent(resource.fhirType(), x -> new HashMap<String, MetadataResource>());
		typedResourcesById.put(resource.getId(), resource);

		Optional<SemanticVersion> version = SemanticVersion.create(resource.getVersion());
		if (!version.isPresent()) {
			throw new IllegalArgumentException(
					String.format("Error processing %s-%s: knowledge artifacts must contain a version and the value must be a valid semantic version string", resource.getName(), resource.getVersion()));
		}

		Map<String, SortedMap<SemanticVersion, MetadataResource>> typedResourcesByName = resourcesByNameByResourceType
				.computeIfAbsent(resource.fhirType(),
						x -> new HashMap<String, SortedMap<SemanticVersion, MetadataResource>>());

		Map<SemanticVersion, MetadataResource> resourceByVersion = typedResourcesByName
				.computeIfAbsent(resource.getName(), key -> new TreeMap<SemanticVersion, MetadataResource>());
		resourceByVersion.put(version.get(), resource);

		if (resource.getUrl() == null) {
			throw new IllegalArgumentException("Knowledge artifacts must contain a url");
		}

		Map<String, SortedMap<SemanticVersion, MetadataResource>> typedResourcesByUrl = resourcesByUrlByResourceType
				.computeIfAbsent(resource.fhirType(),
						x -> new HashMap<String, SortedMap<SemanticVersion, MetadataResource>>());

		resourceByVersion = typedResourcesByUrl.computeIfAbsent(resource.getUrl(),
				key -> new TreeMap<SemanticVersion, MetadataResource>());
		resourceByVersion.put(version.get(), resource);
	}

	/**
	 * Check whether a given artifact name passes the search filters. This allows
	 * implementations to selectively choose which resources when FHIR resources are
	 * mixed with other artifact types (e.g. CQL/ELM).
	 * 
	 * @param fileName    artifact name of the stored resource
	 * @param searchPaths array of name-based filters to apply to the artifact.
	 *                    Artifacts must be of type JSON and have a path prefix that
	 *                    matches at least one of the filters (if provided)
	 * @return true if the file is allowed. Otherwise, false.
	 */
	protected boolean isFileAllowed(String fileName, String... searchPaths) {
		boolean isAllowed = fileName.toLowerCase().endsWith(".json");

		if (isAllowed && searchPaths != null) {
			isAllowed = false;
			for (String path : searchPaths) {
				if (path.equals(FilenameUtils.getPathNoEndSeparator(fileName))) {
					isAllowed = true;
					break;
				}
			}
		}
		return isAllowed;
	}

	/**
	 * Return the requested version or the latest version if no specific version has
	 * been requested.
	 * 
	 * @param resourceVersions resources indexed by version
	 * @param requestedVersion the version requested by the user. If null, the
	 *                         latest version is returned.
	 * @return the requested resource version or the latest if no specific version
	 *         was requested.
	 */
	protected MetadataResource getByVersion(Map<SemanticVersion, MetadataResource> resourceVersions, String requestedVersion) {
		MetadataResource result = null;
		Optional<SemanticVersion> semver = SemanticVersion.create(requestedVersion);
		if (semver.isPresent()) {
			result = resourceVersions.get(semver.get());
		} else {
			if( resourceVersions != null ) {
				Optional<MetadataResource> latest = resourceVersions.values().stream().reduce((x, y) -> y);
				if (latest.isPresent()) {
					result = latest.get();
				}
			}
		}
		return result;
	}

	/**
	 * Lookup a fhir resource by resource type and canonical URL
	 * 
	 * @param fhirType     FHIR resource type
	 * @param canonicalUrl URL in FHIR canonical URL format
	 * @return resolved resource or null if not found
	 */
	protected MetadataResource resolveByCanonicalUrl(String fhirType, String canonicalUrl) {
		MetadataResource result = null;
		
		Pair<String, String> parts = CanonicalHelper.separateParts(canonicalUrl);

		Map<String, SortedMap<SemanticVersion, MetadataResource>> resourcesByUrl = resourcesByUrlByResourceType.get(fhirType);
		if( resourcesByUrl != null ) {
			SortedMap<SemanticVersion, MetadataResource> versions = resourcesByUrl.get(parts.getLeft());
	
			String version = parts.getRight();
	
			result = getByVersion(versions, version);
		} 
		
		return result;
	}

	/**
	 * Lookup a fhir resource by resource type, name, and optional version.
	 * 
	 * @param fhirType FHIR resource type
	 * @param name     FHIR resource name
	 * @param version  FHIR resource version or null if the latest version is
	 *                 desired.
	 * @return resolved resource or null if not found
	 */
	protected MetadataResource resolveByName(String fhirType, String name, String version) {
		SortedMap<SemanticVersion, MetadataResource> versions = resourcesByNameByResourceType.get(fhirType).get(name);

		return getByVersion(versions, version);
	}

	@Override
	public Measure resolveMeasureById(String resourceID) {
		return (Measure) resourcesByIdByResourceType.get("Measure").get(resourceID);
	}

	@Override
	public Measure resolveMeasureByCanonicalUrl(String canonicalUrl) {

		return (Measure) resolveByCanonicalUrl("Measure", canonicalUrl);
	}

	@Override
	public Measure resolveMeasureByName(String name, String version) {
		return (Measure) resolveByName("Measure", name, version);
	}

	@Override
	public Measure resolveMeasureByIdentifier(Identifier identifier, String version) {
		Measure result = null;

		SemanticVersion latestVersion = null;
		for (MetadataResource resource : resourcesByIdByResourceType.get("Measure").values()) {
			Measure measure = (Measure) resource;

			boolean hasId = false;
			for (Identifier candidate : measure.getIdentifier()) {
				if (EqualsBuilder.reflectionEquals(candidate, identifier, new String[] { "system", "value" })) {
					hasId = true;
					break;
				}
			}

			if (hasId) {
				if (version != null && version.equals(measure.getVersion())) {
					result = measure;
					break;
				} else {
					Optional<SemanticVersion> candidateVersion = SemanticVersion.create(measure.getVersion());
					if (candidateVersion.isPresent()) {
						if (latestVersion == null) {
							result = measure;
						} else if (candidateVersion.get().compareTo(latestVersion) > 0) {
							latestVersion = candidateVersion.get();
							result = measure;
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public Library resolveLibraryById(String libraryId) {
		return (Library) resourcesByIdByResourceType.get("Library").get(libraryId);
	}

	@Override
	public Library resolveLibraryByName(String libraryName, String libraryVersion) {
		return (Library) resolveByName("Library", libraryName, libraryVersion);
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
		return (Library) resolveByCanonicalUrl("Library", libraryUrl);
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("Library updates are not supported");
	}
}
