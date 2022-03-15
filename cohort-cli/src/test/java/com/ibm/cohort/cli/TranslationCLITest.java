/*
 *
 *  * (C) Copyright IBM Corp. 2021, 2022
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.BasePatientTest;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

public class TranslationCLITest extends BasePatientTest {
	
	private static final String END_OF_TRANSLATED_LIBRARY = "</library>";

	@Test
	public void basicFunctionalityCheck() throws Exception {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream captureOut = new PrintStream(baos)) {
			System.setOut(captureOut);
			TranslationCLI.main(new String[]{
					"-f", "src/test/resources/cql/basic/Test-1.0.0.cql"
			});
		} finally {
			System.setOut(originalOut);
		}
		String output = new String(baos.toByteArray());
		String[] lines = output.split("\r?\n");
		assertEquals(END_OF_TRANSLATED_LIBRARY, lines[lines.length - 1]);
	}

	@Test
	public void testModelInfo() throws Exception {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintStream captureOut = new PrintStream(baos)) {
			System.setOut(captureOut);
			TranslationCLI.main(new String[]{
					"-f", "src/test/resources/cql/ig-test/Test-1.0.0.cql",
					"-i", "src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml"
			});
		} finally {
			System.setOut(originalOut);
		}
		String output = new String(baos.toByteArray());
		String[] lines = output.split("\r?\n");
		assertEquals(END_OF_TRANSLATED_LIBRARY, lines[lines.length - 1]);
	}
}