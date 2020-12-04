/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.measure.BaseMeasureTest;

public class MeasureCLITest extends BaseMeasureTest {
	private static final String TMP_PARAM_FILE_LOCATION = "target/measure-params.json";
	
	@Test
	public void testCohortMeasureSinglePatient() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient);
		
		Library library = mockLibraryRetrieval("Test", "cql/basic/test.cql");
		
		Measure measure = getCohortMeasure("Test", library, "Female");
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileForSingleMeasure(measure.getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient.getId() 
			}, out);	
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 2, lines.length );
	}

	@Test
	public void testCohortMeasuresMultiplePatients() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient1 = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient1);

		Patient patient2 = getPatient("234", AdministrativeGender.MALE, "1592-14-04");
		mockFhirResourceRetrieval(patient2);

		Patient patient3 = getPatient("888", AdministrativeGender.FEMALE, "1592-14-05");
		mockFhirResourceRetrieval(patient3);

		Library library1 = mockLibraryRetrieval("Test", "cql/basic/test.cql");

		Measure measure = getCohortMeasure("Test", library1, "Male");
		mockFhirResourceRetrieval(measure);

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileFromContents(
				"{\"measureParameters\":[{\"measureId\":\"" + measure.getId() + "\"}," + 
						"{\"measureId\":\"" + measure.getId() + "\",\"parameters\":[" + createParameterString("p1", "integer", "10")+ "]}]}");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient1.getId(),
					"-c", patient2.getId(),
					"-c", patient3.getId()
			}, out);
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}

		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		// First two patients create 7 lines of output each (1 for start of patient context, 3 per measure result).
		// Last patient only creates 2 lines (1 for start of patient context, 1 for line divider since there is no measure report returned).
		assertEquals( output, 16, lines.length );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingResourceParametersFile() throws Exception {
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", "target/garbageFilepciwebocwe8293ivsohvb",
					"-c", "p1"
			}, out);
		} finally {
			tmpFile.delete();
		}
	}
	
	@Test
	public void testProportionRatioSinglePatient() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = new Patient();
		patient.setId("123");
		patient.setGender(AdministrativeGender.MALE);
		
		OffsetDateTime birthDate = OffsetDateTime.now().minusYears(30);
		patient.setBirthDate(Date.from(birthDate.toInstant()));
		
		mockFhirResourceRetrieval(patient);
		
		Library library = mockLibraryRetrieval("Test", "cql/basic/test.cql");
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, "Over the hill");
		
		expectationsByPopulationType.clear();
		expectationsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, 1);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 1);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOR, 0);
		
		Measure measure = getProportionMeasure("Test", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileForSingleMeasure(measure.getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient.getId() 
			}, out);	
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 6, lines.length );
		assertTextPopulationExpectations(lines);
	}
	
	@Test
	public void testProportionRatioMultiplePatients() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient1 = mockPatientRetrieval("123", AdministrativeGender.MALE, 30);
		Patient patient2 = mockPatientRetrieval("456", AdministrativeGender.MALE, 45);
		Patient patient3 = mockPatientRetrieval("789", AdministrativeGender.FEMALE, 45);
		
		Library library = mockLibraryRetrieval("Test", "cql/basic/test.cql");
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, "Over the hill");
		
		expectationsByPopulationType.clear();
		expectationsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, 1);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 1);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOR, 0);
		
		Measure measure = getProportionMeasure("Test", library, expressionsByPopulationType);
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient1.getId(),
					"-c", patient2.getId(),
					"-c", patient3.getId()
			}, out);	
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 14, lines.length );
	}
	
	@Test
	public void testCareGapSinglePatient() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient1 = mockPatientRetrieval("123", AdministrativeGender.MALE, 30);
		
		Library library = mockLibraryRetrieval("Test", "cql/basic/test.cql");
		
		expressionsByPopulationType.clear();
		expressionsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.DENOMINATOR, "Male");
		expressionsByPopulationType.put(MeasurePopulationType.NUMERATOR, "Over the hill");
		
		expectationsByPopulationType.clear();
		expectationsByPopulationType.put(MeasurePopulationType.INITIALPOPULATION, 1);
		expectationsByPopulationType.put(MeasurePopulationType.DENOMINATOR, 1);
		expectationsByPopulationType.put(MeasurePopulationType.NUMERATOR, 0);
		
		Measure measure = getCareGapMeasure("Test", library, expressionsByPopulationType, "Over the hill");
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient1.getId()
			}, out);	
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 7, lines.length );
	}
	
	@Test
	public void testJsonFormattedOutput() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient);
		
		Library library = mockLibraryRetrieval("Test", "cql/basic/test.cql");
		
		Measure measure = getCohortMeasure("Test", library, "Male");
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpResourceParametersFile = createTmpParametersFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-p", tmpResourceParametersFile.getAbsolutePath(),
					"-c", patient.getId(),
					"-f", "JSON"
			}, out);	
		} finally {
			tmpFile.delete();
			tmpResourceParametersFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		assertTrue( output.contains("\"resourceType\": \"MeasureReport\"") );
	}	
	
	protected void assertTextPopulationExpectations(String[] lines) {
		Pattern p = Pattern.compile("Population: (?<code>[^ ]+) = (?<count>[0-9]+)");
		for( String line : lines ) {
			Matcher m = p.matcher(line);
			if( m.matches() ) {
				MeasurePopulationType type = MeasurePopulationType.fromCode( m.group("code") );
				Integer actualCount = Integer.parseInt( m.group("count") );
				assertEquals( type.toCode(), expectationsByPopulationType.get(type), actualCount );
			}
		}
	}
	
	private File createTmpParametersFileForSingleMeasure(String measureId) throws IOException {
		return createTmpParametersFileFromContents("{\"measureParameters\":[{\"measureId\":\"" + measureId + "\"}]}");
	}
	
	private String createParameterString(String name, String type, String value) {
		return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"value\":\"" + value +"\"}";
	}
	
	private File createTmpParametersFileFromContents(String contents) throws IOException {
		File tmpResourceParametersFile = new File(TMP_PARAM_FILE_LOCATION);
		try (Writer w = new FileWriter(tmpResourceParametersFile)) {
			w.write(contents);
		}
		return tmpResourceParametersFile;
	}
}
