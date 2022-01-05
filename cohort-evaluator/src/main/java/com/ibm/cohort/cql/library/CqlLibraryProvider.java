/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

/**
 * Provides a {@link CqlLibrary} when given a {@link CqlLibraryDescriptor}.
 */
public interface CqlLibraryProvider {

    public CqlLibrary getLibrary(CqlLibraryDescriptor cqlResourceDescriptor);

}
