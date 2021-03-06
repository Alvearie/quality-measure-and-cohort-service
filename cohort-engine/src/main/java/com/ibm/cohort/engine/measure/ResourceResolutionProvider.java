/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
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
	
	private static final String MEASURE = "Measure";
	private static final String LIBRARY = "Library";
	
	private Map<String, Map<String, MetadataResource>> resourcesByIdByResourceType = new HashMap<>();
	private Map<String, Map<String, Map<String, MetadataResource>>> resourcesByNameByResourceType = new HashMap<>();
	private Map<String, Map<String, Map<String, MetadataResource>>> resourcesByUrlByResourceType = new HashMap<>();
	
	
	
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
	 */
	protected void processResource(String resourceName, InputStream resourceData, IParser parser) {
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
	 */
	protected void processResource(String resourceName, IBaseResource resource) {

		if (resource instanceof MetadataResource) {
			MetadataResource metadataResource = (MetadataResource) resource;

			if (metadataResource.getId() == null) {
				String id = FilenameUtils.getBaseName(resourceName);
				metadataResource.setIdElement(new IdType(metadataResource.fhirType(), id));
			}

			addToIndexes(metadataResource);
		} else if (resource instanceof Bundle) {
			Bundle bundle = (Bundle) resource;

			for (BundleEntryComponent be : bundle.getEntry()) {
				if (be.getResource() instanceof MetadataResource) {
					MetadataResource metadataResource = (MetadataResource) be.getResource();
					if (metadataResource.getIdElement().getValueAsString() == null) {
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
				.computeIfAbsent(resource.fhirType(), x -> new HashMap<>());
		typedResourcesById.put(resource.getIdElement().getIdPart(), resource);

		String version = resource.getVersion();
		if (version == null || version.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Error processing %s-%s: knowledge artifacts must contain a version", resource.getName(), resource.getVersion()));
		}

		Map<String, Map<String, MetadataResource>> typedResourcesByName = resourcesByNameByResourceType
				.computeIfAbsent(resource.fhirType(),
						x -> new HashMap<>());

		Map<String, MetadataResource> resourceByVersion = typedResourcesByName
				.computeIfAbsent(resource.getName(), key -> new HashMap<>());
		resourceByVersion.put(version, resource);

		if (resource.getUrl() == null) {
			throw new IllegalArgumentException("Knowledge artifacts must contain a url");
		}

		Map<String, Map<String, MetadataResource>> typedResourcesByUrl = resourcesByUrlByResourceType
				.computeIfAbsent(resource.fhirType(),
						x -> new HashMap<>());

		resourceByVersion = typedResourcesByUrl.computeIfAbsent(resource.getUrl(),
				key -> new HashMap<>());
		resourceByVersion.put(version, resource);
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
	 *         was requested. Only resources with semantic versions are considered
	 *         when finding the latest version.
	 */
	protected MetadataResource getByVersion(Map<String, MetadataResource> resourceVersions, String requestedVersion) {
		MetadataResource result = null;
		
		if( resourceVersions != null ) {
			if (requestedVersion != null && !requestedVersion.isEmpty()) {
				result = resourceVersions.get(requestedVersion);
			} else {
				Optional<SemanticVersion> latest = resourceVersions
						.values()
						.stream()
						.map(x -> SemanticVersion.create(x.getVersion()))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.sorted()
						.reduce((x, y) -> y);
				if (latest.isPresent()) {
					result = resourceVersions.get(latest.get().toString());
				}
			}
		}
		return result;
	}

	/**
	 * Lookup a FHIR resource by resource type and canonical URL
	 * 
	 * @param fhirType     FHIR resource type
	 * @param canonicalUrl URL in FHIR canonical URL format
	 * @return resolved resource or null if not found
	 */
	protected MetadataResource resolveByCanonicalUrl(String fhirType, String canonicalUrl) {
		MetadataResource result = null;
		
		Pair<String, String> parts = CanonicalHelper.separateParts(canonicalUrl);

		Map<String, Map<String, MetadataResource>> resourcesByUrl = resourcesByUrlByResourceType.get(fhirType);
		if( resourcesByUrl != null ) {
			Map<String, MetadataResource> versions = resourcesByUrl.get(parts.getLeft());
	
			String version = parts.getRight();
	
			result = getByVersion(versions, version);
		} 
		
		return result;
	}

	/**
	 * Lookup a FHIR resource by resource type, name, and optional version.
	 * 
	 * @param fhirType FHIR resource type
	 * @param name     FHIR resource name
	 * @param version  FHIR resource version or null if the latest version is
	 *                 desired.
	 * @return resolved resource or null if not found
	 */
	protected MetadataResource resolveByName(String fhirType, String name, String version) {
		Map<String, MetadataResource> versions = resourcesByNameByResourceType.get(fhirType).get(name);

		return getByVersion(versions, version);
	}

	@Override
	public Measure resolveMeasureById(String resourceID) {
		return (Measure) getResourceByTypeAndId(MEASURE, resourceID);
	}

	private MetadataResource getResourceByTypeAndId(String resourceType, String resourceID) {
		MetadataResource result = null;
		if( resourceID != null ) {
			Map<String,MetadataResource> resourcesById = resourcesByIdByResourceType.get(resourceType);
			if( resourcesById != null ) {
				result = resourcesById.get(resourceID);
			}
		}
		return result;
	}

	@Override
	public Measure resolveMeasureByCanonicalUrl(String canonicalUrl) {

		return (Measure) resolveByCanonicalUrl(MEASURE, canonicalUrl);
	}

	@Override
	public Measure resolveMeasureByName(String name, String version) {
		return (Measure) resolveByName(MEASURE, name, version);
	}

	@Override
	public Measure resolveMeasureByIdentifier(Identifier identifier, String version) {
		Measure result = null;

		List<MetadataResource> matchesByIdentifier = resourcesByIdByResourceType.get(MEASURE).values().stream().filter( resource -> {
			return ((Measure)resource).getIdentifier().stream().anyMatch( candidate -> {
				return new EqualsBuilder()
					.append(candidate.getSystem(), identifier.getSystem())
					.append(candidate.getValue(), identifier.getValue())
					.isEquals();
				
			});
		}).collect(Collectors.toList());
		
		if( !matchesByIdentifier.isEmpty() ) {
			if( version != null ) {
				// The user requested a specific version, so find it in the list.
				List<MetadataResource> matchesByVersion = matchesByIdentifier.stream()
						.filter( resource -> { 
								return version.equals( resource.getVersion() );
							})
						.collect(Collectors.toList());
				
				if( matchesByVersion.size() == 1 ) {
					result = (Measure) matchesByVersion.get(0);
				} else if( matchesByVersion.size() > 1 ) { 
					throw new IllegalArgumentException(String.format(
							"Failed to resolve measure by identifier; found unexpected number of rows %d matching provided values",
							matchesByVersion.size()));
				}
			} else {
				// Otherwise, find the resource with the newest semantic version. Resources
				// with a null version are discarded.
				Optional<MetadataResource> optional = matchesByIdentifier.stream()
					.map( resource -> {
							return new AbstractMap.SimpleEntry<>(SemanticVersion.create(resource.getVersion()), resource); 
						})
					.filter( entry -> entry.getKey().isPresent() )
					.sorted( (x,y) -> x.getKey().get().compareTo(y.getKey().get()) )
					.map( entry -> entry.getValue() )
					.reduce( (x,y) -> y );
				if( optional.isPresent() ) {
					result = (Measure) optional.get();
				} else { 
					throw new IllegalArgumentException("Failed to resolve measure by identifier; matched identifier, but found no records with non-null version");
				}
			}
		}

		return result;
	}

	@Override
	public Library resolveLibraryById(String libraryId) {
		return (Library) getResourceByTypeAndId(LIBRARY, libraryId);
	}

	@Override
	public Library resolveLibraryByName(String libraryName, String libraryVersion) {
		return (Library) resolveByName(LIBRARY, libraryName, libraryVersion);
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
		return (Library) resolveByCanonicalUrl(LIBRARY, libraryUrl);
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("Library updates are not supported");
	}
}
