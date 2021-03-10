package com.ibm.cohort.engine.api.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.parser.IParser;

public class TestHelper {
	public static void createMeasureArtifact(File outFile, IParser parser, Measure measure, Library library)
			throws IOException, FileNotFoundException {
		try( OutputStream os = new FileOutputStream(outFile) ) {
			createMeasureArtifact(os, parser, measure, library);
		}
	}

	public static void createMeasureArtifact(OutputStream os, IParser parser, Measure measure, Library library)
			throws IOException {
		try( ZipOutputStream zos = new ZipOutputStream(os) ) {
			ZipEntry libEntry = new ZipEntry("fhirResources/libraries/test_library-1.0.0.json");
			zos.putNextEntry(libEntry);
			zos.write( parser.encodeResourceToString( library ).getBytes(StandardCharsets.UTF_8) );
			zos.closeEntry();
			
			ZipEntry measureEntry = new ZipEntry("fhirResources/test_measure-1.0.0.json");
			zos.putNextEntry(measureEntry);
			zos.write( parser.encodeResourceToString( measure ).getBytes(StandardCharsets.UTF_8) );
			zos.closeEntry();
		}
	}
}
