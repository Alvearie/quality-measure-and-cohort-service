/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

public class ZipStreamLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInZipSuccess() throws Exception {
		try ( ZipInputStream zip = new ZipInputStream( new FileInputStream( "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip") ) ) {
		
			ZipStreamLibrarySourceProvider provider = new ZipStreamLibrarySourceProvider(zip);
			try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("FHIRHelpers") ) ) { 
				assertNotNull( is );
			}
		}
	}
}
