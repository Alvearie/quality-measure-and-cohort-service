/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import org.hl7.elm.r1.VersionedIdentifier;

/**
 * Conversion strategy for converting a filename as String into a 
 * VersionedIdentifier for use in the LibrarySourceProvider.
 */
@FunctionalInterface
public interface FilenameToVersionedIdentifierStrategy {
	public VersionedIdentifier filenameToVersionedIdentifier( String filename );
}
