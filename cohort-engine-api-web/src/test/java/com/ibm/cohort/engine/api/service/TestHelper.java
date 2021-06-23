/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.codesystems.MeasureScoring;

import com.ibm.cohort.engine.helpers.CanonicalHelper;

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
	
	public static Measure getTemplateMeasure(Library library) {
		Calendar c = Calendar.getInstance();
		c.set(2019, 07, 04, 0, 0, 0);
		Date startDate = c.getTime();
		
		c.add( Calendar.YEAR, +1);
		Date endDate = c.getTime();
		
		Measure measure = new Measure();
		measure.setId("measureId");
		measure.setName("test_measure");
		measure.setVersion("1.0.0");
		measure.setUrl("http://ibm.com/health/Measure/test_measure");
		measure.setScoring(new CodeableConcept().addCoding(new Coding().setCode(MeasureScoring.COHORT.toCode())));
		measure.setEffectivePeriod(new Period().setStart(startDate).setEnd(endDate));
		measure.addLibrary(CanonicalHelper.toCanonicalUrl(library));
		measure.addGroup().addPopulation().setCode(new CodeableConcept().addCoding(new Coding().setSystem("http://hl7.org/fhir/measure-population").setCode("initial-population"))).setCriteria(new Expression().setExpression("Adult"));
		return measure;
	}

	public static Library getTemplateLibrary() {
		Library library = new Library();
		library.setId("libraryId");
		library.setName("test_library");
		library.setVersion("1.0.0");
		library.setUrl("http://ibm.com/health/Library/test_library");
		library.addContent().setContentType("text/cql").setData("library test_library version '1.0.0'\nparameter AgeOfMaturation default 18\nusing FHIR version '4.0.0'\ncontext Patient\ndefine Adult: AgeInYears() >= \"AgeOfMaturation\"".getBytes());
		return library;
	}

	//todo rename
	public static Library getBasicLibrary2() {
		Library library = new Library();
		library.setId("libraryId");
		library.setName("test_library");
		library.setVersion("1.0.0");
		library.setUrl("http://ibm.com/health/Library/test_library");
		library.addContent().setContentType("text/cql").setData("library test_library version '1.0.0'\nusing FHIR version '4.0.0'\ncontext Patient\ndefine Male: Patient.gender.value = 'male'".getBytes());
		return library;
	}
	
	public static ByteArrayInputStream emptyZip() throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try ( ZipOutputStream zos = new ZipOutputStream(baos) ) { }
		return new ByteArrayInputStream( baos.toByteArray() );
	}
}
