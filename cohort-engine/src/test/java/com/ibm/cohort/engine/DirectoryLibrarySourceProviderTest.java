/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.file.Paths;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

import com.ibm.cohort.cql.OptimizedCqlLibraryReader;
import com.ibm.cohort.translator.provider.CqlTranslationProvider;
import com.ibm.cohort.translator.provider.InJVMCqlTranslationProvider;


public class DirectoryLibrarySourceProviderTest {
	@Test
	public void testLibraryFoundInDirectorySuccess() throws Exception {
		DirectoryLibrarySourceProvider provider = new DirectoryLibrarySourceProvider(Paths.get("src/test/resources/cql/basic"));
		try( InputStream is = provider.getLibrarySource( new VersionedIdentifier().withId("test") ) ) {
			assertNotNull( is );
			
			CqlTranslationProvider tx = new InJVMCqlTranslationProvider();
			Library library = OptimizedCqlLibraryReader.read( tx.translate( is ) );
			assertEquals( "Test", library.getIdentifier().getId() );
		}
	}
}
