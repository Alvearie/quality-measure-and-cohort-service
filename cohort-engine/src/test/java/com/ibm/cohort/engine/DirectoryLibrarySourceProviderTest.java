/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.file.Paths;

import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

public class DirectoryLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInDirectorySuccess() throws Exception {
		DirectoryLibrarySourceProvider provider = new DirectoryLibrarySourceProvider(Paths.get("src/test/resources/cql/basic"));
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("test") ) ) {
			assertNotNull( is );
		}
	}
}
