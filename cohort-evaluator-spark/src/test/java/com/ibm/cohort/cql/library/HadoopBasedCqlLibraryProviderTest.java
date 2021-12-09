/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

public class HadoopBasedCqlLibraryProviderTest {
	@Test
	public void testListAndRetrieveSuccess() {
		CqlLibraryProvider provider = new HadoopBasedCqlLibraryProvider(new Path("src/test/resources/library-provider"), new Configuration());
		Collection<CqlLibraryDescriptor> libraries = provider.listLibraries();
		assertEquals(2, libraries.size());

		CqlLibraryDescriptor expectedCql = new CqlLibraryDescriptor()
				.setLibraryId("CohortHelpers")
				.setVersion("1.0.0")
				.setFormat(CqlLibraryDescriptor.Format.CQL);

		assertTrue("Missing expected library", libraries.contains(expectedCql));

		CqlLibraryDescriptor expectedElm = new CqlLibraryDescriptor()
				.setLibraryId("MyCQL")
				.setVersion("1.0.0")
				.setFormat(CqlLibraryDescriptor.Format.ELM);

		assertTrue("Missing expected library", libraries.contains(expectedElm));
	}

	@Test
	public void testGetLibrarySuccess() {
		CqlLibraryProvider provider = new HadoopBasedCqlLibraryProvider(new Path("src/test/resources/library-provider"), new Configuration());
		
		CqlLibraryDescriptor expectedCql = new CqlLibraryDescriptor()
				.setLibraryId("CohortHelpers")
				.setVersion("1.0.0")
				.setFormat(CqlLibraryDescriptor.Format.CQL);

		CqlLibrary library = provider.getLibrary(expectedCql);
		assertNotNull("Missing expected library", library);
		assertEquals(expectedCql, library.getDescriptor());
	}

	@Test
	public void testGetLibraryNotFound() {
		CqlLibraryProvider provider = new HadoopBasedCqlLibraryProvider(new Path("src/test/resources/library-provider"), new Configuration());

		CqlLibraryDescriptor expectedCql = new CqlLibraryDescriptor()
				.setLibraryId("NotThere")
				.setVersion("1.0.0")
				.setFormat(CqlLibraryDescriptor.Format.CQL);

		assertNull("Found unexpected library", provider.getLibrary(expectedCql));
	}
}