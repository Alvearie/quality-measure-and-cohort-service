/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;

import com.ibm.cohort.version.SemanticVersion;

import ca.uhn.fhir.parser.IParser;

public abstract class ResourceResolutionProvider implements LibraryResolutionProvider<Library>, MeasureResolutionProvider<Measure> {
	private Map<String, Map<String,MetadataResource>> resourcesByIdByResourceType = new HashMap<>();
	private Map<String, Map<String,MetadataResource>> resourcesByNameVersionByResourceType = new HashMap<>();
	private Map<String, Map<String,MetadataResource>> resourcesByUrlByResourceType = new HashMap<>();
	
	protected void processResource(String resourcePath, InputStream resourceData, IParser parser) throws IOException {
		IBaseResource resource = parser.parseResource( resourceData );

		if( resource instanceof MetadataResource ) {
			MetadataResource metadataResource = (MetadataResource) resource;
					
			if( metadataResource.getId() == null ) { 
				metadataResource.setId( FilenameUtils.getBaseName(resourcePath) );
			}
			
			addToIndexes(metadataResource);
		} else if( resource instanceof Bundle ) {
			Bundle bundle = (Bundle) resource;
			
			for( BundleEntryComponent be : bundle.getEntry() ) {
				if( be.getResource() instanceof MetadataResource ) {
					MetadataResource metadataResource = (MetadataResource) be.getResource();
					if( metadataResource.getId() == null ) {
						throw new IllegalArgumentException("Bundle resources must contain an ID value");
					}
					
					addToIndexes(metadataResource);
				}
			}
		}
	}

	protected void addToIndexes(MetadataResource resource) {
		Map<String,MetadataResource> typedResourcesById = resourcesByIdByResourceType.computeIfAbsent( resource.fhirType(), x -> new HashMap<String,MetadataResource>() );
		typedResourcesById.put( resource.getId(), resource );
		
		Map<String,MetadataResource> typedResourcesByNameVersion = resourcesByNameVersionByResourceType.computeIfAbsent( resource.fhirType(), x -> new HashMap<String,MetadataResource>() );
		typedResourcesByNameVersion.put( computeNameVersionKey(resource.getName(), resource.getVersion()), resource);
		
		Map<String,MetadataResource> typedResourcesByUrl = resourcesByUrlByResourceType.computeIfAbsent( resource.fhirType(), x -> new HashMap<String,MetadataResource>() );
		typedResourcesByUrl.put( resource.getUrl(), resource);
	}

	protected boolean isFileAllowed(String fileName, String... searchPaths) {
		boolean isAllowed = fileName.toLowerCase().endsWith(".json");
		
		if( isAllowed && searchPaths != null ) {
			isAllowed = false;
			for( String path : searchPaths ) {
				if( path.equals( FilenameUtils.getPathNoEndSeparator( fileName ) ) ) {
					isAllowed = true;
					break;
				}
			}
		}
		return isAllowed;
	}
	
	protected String computeNameVersionKey(String name, String version) {
		String key = name;
		if( version != null ) {
			key = key + "|" + version;
		}
		return key;
	}

	@Override
	public Measure resolveMeasureById(String resourceID) {
		return (Measure) resourcesByIdByResourceType.get("Measure").get(resourceID);
	}

	@Override
	public Measure resolveMeasureByCanonicalUrl(String url) {
		return (Measure) resourcesByUrlByResourceType.get("Measure").get(url);
	}

	@Override
	public Measure resolveMeasureByName(String name, String version) {
		return (Measure) resourcesByUrlByResourceType.get("Measure").get(computeNameVersionKey(name,version));
	}

	@Override
	public Measure resolveMeasureByIdentifier(Identifier identifier, String version) {
		Measure result = null;
		
		SemanticVersion latestVersion = null;
		for( MetadataResource resource : resourcesByIdByResourceType.get("Measure").values() ) {
			Measure measure = (Measure) resource;

			boolean hasId = false;
			for( Identifier candidate : measure.getIdentifier() ) {
				if( EqualsBuilder.reflectionEquals(candidate, identifier, new String[] { "system", "value" } ) ) {
					hasId = true;
					break;
				}
			}
			
			if( hasId ) { 
				if( version != null && version.equals(measure.getVersion()) ) {
					result = measure;
					break;
				} else { 
					Optional<SemanticVersion> candidateVersion = SemanticVersion.create(measure.getVersion());
					if( candidateVersion.isPresent() ) {
						if( latestVersion == null ) {
							result = measure;
						} else if( candidateVersion.get().compareTo(latestVersion) > 0 ){ 
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
		return (Library) resourcesByNameVersionByResourceType.get("Library").get(computeNameVersionKey(libraryName,libraryVersion));
	}

	@Override
	public Library resolveLibraryByCanonicalUrl(String libraryUrl) {
		return (Library) resourcesByUrlByResourceType.get("Library").get(libraryUrl);
	}

	@Override
	public void update(Library library) {
		throw new UnsupportedOperationException("Library updates are not supported");
	}
}
