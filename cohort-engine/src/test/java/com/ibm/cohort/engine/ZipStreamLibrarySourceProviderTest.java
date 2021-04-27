/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

public class ZipStreamLibrarySourceProviderTest {
//	@Test
//	public void testLibraryFoundInZipSuccess() throws Exception {
//		try ( ZipInputStream zip = new ZipInputStream( new FileInputStream( "src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip") ) ) {
//
//			ZipStreamLibrarySourceProvider provider = new ZipStreamLibrarySourceProvider(zip);
//			try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("FHIRHelpers") ) ) {
//				assertNotNull( is );
//
//				CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
//				Library library = tx.translate( is );
//				assertEquals( "FHIRHelpers", library.getIdentifier().getId() );
//			}
//		}
//	}
	
	@Test
	public void testLibraryFoundInZipWithSearchPathsSuccess() throws Exception {
		try ( ZipInputStream zip = new ZipInputStream( new FileInputStream( "src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip" ) ) ) {
		
			ZipStreamLibrarySourceProvider provider = new ZipStreamLibrarySourceProvider(zip, "CDSexport");
			try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("COL_InitialPop").withVersion("1.0.0") ) ) { 
				assertNotNull( is );
				
				CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
				Library library = tx.translate( is );
				assertEquals( "COL_InitialPop", library.getIdentifier().getId() );
			}
		}
	}
	
	@Test
	public void testLibraryFoundInZipWithSearchPathsMissingError() throws Exception {
		try ( ZipInputStream zip = new ZipInputStream( new FileInputStream( "src/test/resources/cql/zip-structured/col_colorectal_cancer_screening_v1_0_0.zip" ) ) ) {
		
			ZipStreamLibrarySourceProvider provider = new ZipStreamLibrarySourceProvider(zip, "deploypackage");
			try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("COL_InitialPop").withVersion("1.0.0") ) ) { 
				assertNull( "Unexpectedly found resource", is );
			}
		}
	}
}
