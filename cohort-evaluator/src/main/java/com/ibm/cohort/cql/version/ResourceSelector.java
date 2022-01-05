/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.version;

import com.ibm.cohort.version.SemanticVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * Selects a specific resource from a collection of resources based on
 * their versions.
 *
 * <p> It is expected that the provided {@link VersionHandler} will return
 * strings in the form of a Semantic Version (e.g. 1.0.0).
 *
 * @param <T> The resource type to select.
 */
public class ResourceSelector<T> {

    private final VersionHandler<T> versionHandler;

    public ResourceSelector(VersionHandler<T> versionHandler) {
        this.versionHandler = versionHandler;
    }

    public T selectSpecificVersionOrLatest(Collection<T> resources, String version) {
        T retVal;
        if (StringUtils.isEmpty(version)) {
            retVal = selectLatest(resources);
        }
        else {
            retVal = selectSpecificVersion(resources, version);
        }
        return retVal;
    }

    private T selectLatest(Collection<T> resources) {
        T retVal = null;
        SemanticVersion latestVersion = null;
        for (T resource : resources) {
            String rawVersion = versionHandler.getVersion(resource);
            Optional<SemanticVersion> possibleVersion = SemanticVersion.create(rawVersion);
            if (possibleVersion.isPresent()){
                SemanticVersion version = possibleVersion.get();
                if (latestVersion == null || latestVersion.compareTo(version) < 0) {
                    latestVersion = version;
                    retVal = resource;
                }
            }
        }
        return retVal;
    }

    private T selectSpecificVersion(Collection<T> resources, String version) {
        T retVal = null;

        for (T resource : resources) {
            String resourceVersion = versionHandler.getVersion(resource);
            if (version.equals(resourceVersion)) {
                if (retVal == null) {
                    retVal = resource;
                }
                else {
                    throw new IllegalArgumentException("Multiple resources found with specified version: " + version);
                }
            }
        }

        return retVal;
    }

}
