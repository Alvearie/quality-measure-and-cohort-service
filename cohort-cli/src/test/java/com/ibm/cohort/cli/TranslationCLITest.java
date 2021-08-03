/*
 *
 *  * (C) Copyright IBM Corp. 2021
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

	@Test
	public void basicFunctionalityCheck() throws Exception {
		FhirServerConfig fhirConfig = getFhirServerConfig();

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				TranslationCLI.main(new String[]{
						"-d", tmpFile.getAbsolutePath(),
						"-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/basic/test.cql"
				});
			}
			finally {
				System.setOut(originalOut);
			}
			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");
			assertEquals(2, lines.length);
		}
		finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testModelInfo() throws Exception {
		FhirServerConfig fhirConfig = getFhirServerConfig();

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(fhirConfig));
		}

		try {
			PrintStream originalOut = System.out;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintStream captureOut = new PrintStream(baos)) {
				System.setOut(captureOut);
				TranslationCLI.main(new String[]{
						"-d", tmpFile.getAbsolutePath(),
						"-t", tmpFile.getAbsolutePath(),
						"-f", "src/test/resources/cql/ig-test/test.cql",
						"-i", "src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml"
				});
			}
			finally {
				System.setOut(originalOut);
			}
			String output = new String(baos.toByteArray());
			String[] lines = output.split("\r?\n");
			assertEquals(2, lines.length);
		}
		finally {
			tmpFile.delete();
		}
	}
}