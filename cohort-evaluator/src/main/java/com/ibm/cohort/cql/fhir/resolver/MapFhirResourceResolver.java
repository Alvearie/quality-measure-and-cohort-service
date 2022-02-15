/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.fhir.resolver;

import com.ibm.cohort.cql.version.ResourceSelector;
import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.helpers.CanonicalHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link FhirResourceResolver} that stores resources provided via direct calls
 * to the {@link #addResource(Object)} or {@link #addResources(Iterable)} methods.
 *
 * <p> Resources are stored in {@link Map}s for fast retrieval.
 *
 * @param <T> The resource type to resolve.
 * @param <I> The identifier type found on {@link T}.
 */
public class MapFhirResourceResolver<T, I> implements FhirResourceResolver<T> {

    private final ResourceFieldHandler<T, I> fieldHandler;
    private final ResourceSelector<T> resourceSelector;

    private final Map<String, T> idMap = new HashMap<>();
    private final Map<String, List<T>> nameMap = new HashMap<>();
    private final Map<String, List<T>> urlMap = new HashMap<>();
    private final Map<String, List<T>> identifierMap = new HashMap<>();

    public MapFhirResourceResolver(ResourceFieldHandler<T, I> fieldHandler, ResourceSelector<T> resourceSelector) {
        this.fieldHandler = fieldHandler;
        this.resourceSelector = resourceSelector;
    }

    @Override
    public T resolveById(String id) {
        return idMap.get(id);
    }

    @Override
    public T resolveByName(String name, String version) {
        List<T> resources = nameMap.getOrDefault(name, Collections.emptyList());
        return resourceSelector.selectSpecificVersionOrLatest(resources, version);
    }

    @Override
    public T resolveByCanonicalUrl(String canonicalUrl) {
        Pair<String, String> splitUrl = CanonicalHelper.separateParts(canonicalUrl);
        List<T> resources = urlMap.getOrDefault(splitUrl.getLeft(), Collections.emptyList());
        return resourceSelector.selectSpecificVersionOrLatest(resources, splitUrl.getRight());
    }

    @Override
    public T resolveByIdentifier(String value, String system, String version) {
        String key = createIdentifierMapKey(value, system);
        List<T> resources = identifierMap.getOrDefault(key, Collections.emptyList());
        return resourceSelector.selectSpecificVersionOrLatest(resources, version);
    }

    public void addResources(Iterable<T> resources) {
        for (T resource : resources) {
            addResource(resource);
        }
    }

    public void addResource(T resource) {
        String id = fieldHandler.getId(resource);
        idMap.put(id, resource);

        String shortenedId = getShortenedId(id);
        if (!StringUtils.isEmpty(shortenedId)) {
            idMap.put(getShortenedId(id), resource);
        }

        String name = fieldHandler.getName(resource);
        nameMap.computeIfAbsent(name, x -> new ArrayList<>()).add(resource);

        String url = fieldHandler.getUrl(resource);
        urlMap.computeIfAbsent(url, x -> new ArrayList<>()).add(resource);

        List<I> identifiers = fieldHandler.getIdentifiers(resource);
        for (I identifier : identifiers) {
            String idValue = fieldHandler.getIdentifierValue(identifier);
            String idSystem = fieldHandler.getIdentifierSystem(identifier);
            String identifierKey = createIdentifierMapKey(idValue, idSystem);
            identifierMap.computeIfAbsent(identifierKey, x -> new ArrayList<>()).add(resource);
        }
    }

    private String getShortenedId(String id) {
        int lastSlashIndex = id.lastIndexOf('/');
        boolean validIndex = lastSlashIndex > -1 && lastSlashIndex < id.length();
        return validIndex ? id.substring(lastSlashIndex + 1) : "";
    }

    private String createIdentifierMapKey(String value, String system) {
        return value + "-" + system;
    }

}
