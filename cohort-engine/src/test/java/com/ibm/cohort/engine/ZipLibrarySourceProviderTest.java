/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.zip.ZipFile;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

public class ZipLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInZipSuccess() throws Exception {
		ZipFile zip = new ZipFile( "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");

		ZipLibrarySourceProvider provider = new ZipLibrarySourceProvider(zip);
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("Breast-Cancer-Screening") ) ) {
			assertNotNull( is );

			CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
			Library library = tx.translate( is );
			assertEquals( "Breast-Cancer-Screening", library.getIdentifier().getId() );
		}
	}

	@Test
	public void testLibraryFoundInZipWithSearchPathsSuccess() throws Exception {
		ZipFile zip = new ZipFile( "src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip");
		
		ZipLibrarySourceProvider provider = new ZipLibrarySourceProvider(zip, "CDSexport");
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("COL_InitialPop").withVersion("1.0.0") ) ) { 
			assertNotNull( "No library source found", is );
			
			CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
			Library library = tx.translate( is );
			assertEquals( "COL_InitialPop", library.getIdentifier().getId() );
		}
	}
	
	@Test
	public void testLibraryFoundInZipWithSearchPathsMissingError() throws Exception {
		ZipFile zip = new ZipFile( "src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip");
		
		ZipLibrarySourceProvider provider = new ZipLibrarySourceProvider(zip, "deploypackage");
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("FHIRHelpers") ) ) { 
			assertNull("Found an unexpected library resource", is);
		}
	}
}
