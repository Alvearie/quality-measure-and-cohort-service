/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.zip.ZipFile;

import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

public class ZipLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInZipSuccess() throws Exception {
		ZipFile zip = new ZipFile( "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");
		
		ZipLibrarySourceProvider provider = new ZipLibrarySourceProvider(zip);
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("FHIRHelpers") ) ) { 
			assertNotNull( is );
		}
	}
}
