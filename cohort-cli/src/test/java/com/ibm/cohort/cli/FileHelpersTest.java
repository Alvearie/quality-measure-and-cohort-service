/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class FileHelpersTest {
	
	@Test
	public void path_with_zip_ext___is_zip()  throws Exception {
		runTest(".zip", true);
	}
	
	@Test
	public void path_with_cql_ext___is_not_zip() throws Exception {
		runTest(".cql", false);
	}
	
	protected void runTest( String suffix, boolean expectation ) throws IOException {
		Path file = Files.createTempFile("temp", suffix);
		try {
			assertEquals( expectation, FileHelpers.isZip(file) );
		} finally {
			Files.delete(file);
		}
	}
}
