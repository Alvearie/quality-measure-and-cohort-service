/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.opencds.cqf.common.evaluation.MeasurePopulationType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.helpers.CanonicalHelper;
import com.ibm.cohort.engine.measure.BaseMeasureTest;

public class MeasureCLITest extends BaseMeasureTest {
	private static final String TMP_MEASURE_CONFIG_FILE_LOCATION = "target/measure-configurations.json";
	
	@Test
	public void testCohortMeasureSinglePatientJsonInput() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient);
		
		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");
		
		Measure measure = getCohortMeasure("Test", library, "Female");
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure(measure.getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient.getId() 
			}, out);	
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 4, lines.length );
	}

	@Test
	public void testCohortMeasureByIDSinglePatientCommandLineInput() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");

		Measure measure = getCohortMeasure("Test", library, "Female");
		mockFhirResourceRetrieval(measure);

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
					"-p", "p1:interval:decimal,1.0,100.5",
					"-p", "p2:integer:1",
					"-r", measure.getId(),
					"-c", patient.getId()
			}, out);
		} finally {
			tmpFile.delete();
		}

		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 4, lines.length );
	}
	
	@Test
	public void testCohortMeasureByURLSinglePatientCommandLineInput() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient);

		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");

		Measure measure = getCohortMeasure("Test", library, "Female");
		mockMeasureRetrieval(measure);

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
					"-p", "p1:interval:decimal,1.0,100.5",
					"-p", "p2:integer:1",
					"-r", CanonicalHelper.toCanonicalUrl(measure),
					"-c", patient.getId()
			}, out);
		} finally {
			tmpFile.delete();
		}

		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 4, lines.length );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultipleParametersInOneString() throws Exception{
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
					"-p", "p1:interval:decimal,1.0,100.5,p2:integer:1",
					"-r", "1234",
					"-c", "54321"
			}, out);
		} finally {
			tmpFile.delete();
		}
	}

	@Test
	public void testCohortMeasuresMultiplePatientsJsonInputByUrl() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());

		Patient patient1 = getPatient("123", AdministrativeGender.MALE, "1592-14-03");
		mockFhirResourceRetrieval(patient1);

		Patient patient2 = getPatient("234", AdministrativeGender.MALE, "1592-14-04");
		mockFhirResourceRetrieval(patient2);

		Patient patient3 = getPatient("888", AdministrativeGender.FEMALE, "1592-14-05");
		mockFhirResourceRetrieval(patient3);

		Library library1 = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");

		Measure measure = getCohortMeasure("Test", library1, "Male");
		mockMeasureRetrieval(measure);

		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileFromContents(
				"{\"measureConfigurations\":[{\"measureId\":\"" + measure.getUrl() + "\"}," + 
						"{\"measureId\":\"" + measure.getId() + "\",\"parameters\":[" + createParameterString("p1", "integer", "10")+ "]}]}");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient1.getId(),
					"-c", patient2.getId(),
					"-c", patient3.getId()
			}, out);
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
		}

		String output = new String(baos.toByteArray());
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 21, lines.length );
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
					"-j", "target/garbageFilepciwebocwe8293ivsohvb",
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
		
		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");
		
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

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure(measure.getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient.getId() 
			}, out);	
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
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
		
		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");
		
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

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient1.getId(),
					"-c", patient2.getId(),
					"-c", patient3.getId()
			}, out);	
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		
		String[] lines = output.split(System.getProperty("line.separator"));
		assertEquals( output, 18, lines.length );
	}
	
	@Test
	public void testCareGapSinglePatient() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient1 = mockPatientRetrieval("123", AdministrativeGender.MALE, 30);
		
		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");
		
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

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient1.getId()
			}, out);	
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
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
		
		Library library = mockLibraryRetrieval("Test", DEFAULT_RESOURCE_VERSION, "cql/basic/test.cql");
		
		Measure measure = getCohortMeasure("Test", library, "Male");
		mockFhirResourceRetrieval(measure);
		
		File tmpFile = new File("target/fhir-stub.json");
		ObjectMapper om = new ObjectMapper();
		try (Writer w = new FileWriter(tmpFile)) {
			w.write(om.writeValueAsString(getFhirServerConfig()));
		}

		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure(measure.getId());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		try {
			MeasureCLI cli = new MeasureCLI();
			cli.runWithArgs(new String[] {
					"-d", tmpFile.getAbsolutePath(),
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-c", patient.getId(),
					"-f", "JSON"
			}, out);	
		} finally {
			tmpFile.delete();
			tmpMeasureConfigurationsFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		assertTrue( output.contains("\"resourceType\": \"MeasureReport\"") );
	}
	
	@Test
	public void testZipFileKnowledgeArtifacts() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, 65);
		mockFhirResourceRetrieval(patient);
		
		
		Bundle emptyBundle = getBundle();
		mockFhirResourceRetrieval(get(urlMatching("/Condition.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Procedure.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Observation.*")), emptyBundle);
		
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
					"-m", "src/test/resources/cql/measure-zip/col_colorectal_cancer_screening_1.0.0.zip",					
					"-r", "Measure/measure-COL_ColorectalCancerScreening-1.0.0",
					"--filter", "fhirResources",
					"-c", patient.getId(),
					"-f", "JSON"
			}, out);	
		} finally {
			tmpFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		assertTrue( output.contains("\"resourceType\": \"MeasureReport\"") );
		assertFalse( "Found null string in output", output.contains("null/") );
	}
	
	@Test
	public void testZipFileInputExtraFolders() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, 65);
		mockFhirResourceRetrieval(patient);
		
		
		Bundle emptyBundle = getBundle();
		mockFhirResourceRetrieval(get(urlMatching("/Condition.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Procedure.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Observation.*")), emptyBundle);
		
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
					"-m", "src/test/resources/cql/measure-zip/simple_age_measure_v2_2_2.zip",					
					"-r", "http://ibm.com/health/Measure/SimpleAgeMeasure|2.2.2",
					"--filter", "fhirResources",
					"--filter", "fhirResources/libraries",
					"-c", patient.getId(),
					"-f", "JSON"
			}, out);	
		} finally {
			tmpFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		assertTrue( output.contains("\"resourceType\": \"MeasureReport\"") );
	}
	
	@Test
	public void testFolderKnowledgeArtifacts() throws Exception {
		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		
		Patient patient = getPatient("123", AdministrativeGender.MALE, 65);
		mockFhirResourceRetrieval(patient);
		
		
		Bundle emptyBundle = getBundle();
		mockFhirResourceRetrieval(get(urlMatching("/Condition.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Procedure.*")), emptyBundle);
		mockFhirResourceRetrieval(get(urlMatching("/Observation.*")), emptyBundle);
		
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
					"-m", "src/test/resources/cql/measure-folders",					
					"-r", "http://ibm.com/health/Measure/measure-COL_ColorectalCancerScreening|1.0.0",
					"--filter", "fhirResources",
					"-c", patient.getId(),
					"-f", "JSON"
			}, out);	
		} finally {
			tmpFile.delete();
		}
		
		String output = new String(baos.toByteArray());
		System.out.println(output);
		assertTrue( output.contains("\"resourceType\": \"MeasureReport\"") );
		assertFalse( "Found null string in output", output.contains("null/") );
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testMissingRequiredArguments() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		MeasureCLI cli = new MeasureCLI();
		cli.runWithArgs(new String[]{
				"-d", "",
				"-c", "1234",
				"-f", "JSON"
		}, out);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExclusiveArgumentsBothSpecified() throws Exception {
		File tmpMeasureConfigurationsFile = createTmpConfigurationsFileForSingleMeasure("12345");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		MeasureCLI cli = new MeasureCLI();
		try {
			cli.runWithArgs(new String[]{
					"-d", "",
					"-j", tmpMeasureConfigurationsFile.getAbsolutePath(),
					"-r", "12345",
					"-c", "1234",
					"-f", "JSON"
			}, out);
		} finally {
			tmpMeasureConfigurationsFile.delete();
		}
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
	
	private File createTmpConfigurationsFileForSingleMeasure(String measureId) throws IOException {
		return createTmpConfigurationsFileFromContents("{\"measureConfigurations\":[{\"measureId\":\"" + measureId + "\"}]}");
	}
	
	private String createParameterString(String name, String type, String value) {
		return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"valueset\":\"" + value +"\"}";
	}
	
	private File createTmpConfigurationsFileFromContents(String contents) throws IOException {
		File tmpMeasureConfigurationsFile = new File(TMP_MEASURE_CONFIG_FILE_LOCATION);
		try (Writer w = new FileWriter(tmpMeasureConfigurationsFile)) {
			w.write(contents);
		}
		return tmpMeasureConfigurationsFile;
	}
}
