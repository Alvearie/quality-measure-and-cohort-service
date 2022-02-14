/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.version;

/**
 * Handles the version field from the specified resource.
 *
 * <p> The format of the version can be any valid String, but other classes
 * may expect the returned string to match the Semantic Version format.
 *
 * @param <T> The resource to handle the version from.
 */
public interface VersionFieldHandler<T> {

    String getVersion(T resource);

}
