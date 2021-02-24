package com.ibm.cohort.tooling.fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.ValueSet;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class VSACValueSetImporter {

	public static final class ValueSetImporterArguments {
		@Parameter(names = {"-m",
				"--measure-server"}, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.", required = true)
		File measureServerConfigFile;

		@Parameter(names = {"-h", "--help"}, description = "Show this help", help = true)
		boolean isDisplayHelp;

		@Parameter(description = "The list of value set spreadsheets to import", required = true)
		List<String> spreadsheets;
	}

	private static class ValueSetArtifact {

		MetadataResource resource;
		String url;
		String name;
		String id;
	}

	private IGenericClient client;
	private IParser parser;

	private VSACValueSetImporter(IGenericClient client) {
		this.client = client;
		this.parser = client.getFhirContext().newJsonParser();
	}

	private void importArtifacts(List<ValueSetArtifact> valueSetArtifacts) {

		for (ValueSetArtifact valueSetArtifact : valueSetArtifacts) {
			Bundle bundle = client.search().forResource(ValueSet.class).where(ValueSet.URL.matches().value(valueSetArtifact.url))
					.returnBundle(Bundle.class).execute();
			if (bundle.getEntry().size() > 0) {
				valueSetArtifact.id = bundle.getEntryFirstRep().getResource().getIdElement().getIdPart();
			} else {
				MethodOutcome outcome = client.create().resource(parser.encodeResourceToString(valueSetArtifact.resource)).execute();
				if (outcome.getCreated()) {
					valueSetArtifact.id = outcome.getId().getIdPart();
				}
			}
		}
	}

	private ValueSetArtifact createArtifact(InputStream is) throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet mainSheet = wb.getSheetAt(0);
		ValueSet valueSet = new ValueSet();
		for (Row currentRow : mainSheet) {
			if(currentRow.getCell(0) != null && currentRow.getCell(1) != null) {
				String value = currentRow.getCell(1).getStringCellValue();
				switch (currentRow.getCell(0).getStringCellValue().toLowerCase()) {
					case "value set name":
						valueSet.setName(value);
						valueSet.setTitle(value);
						break;
					case "oid":
						valueSet.setId(value);
						String url = "http://cts.nlm.nih.gov/fhir/ValueSet/" + value;
						valueSet.setUrl(url);
						break;
					case "definition version":
						valueSet.setVersion(value);
						break;
					default:
						break;
				}
			}
		}
		XSSFSheet expansionSheet = wb.getSheetAt(1);
		ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
		boolean inCodesSection = false;
		HashMap<String, List<ValueSet.ConceptReferenceComponent>> codeSystemToCodes = new HashMap<>();
		for(Row currentRow : expansionSheet){
			if (inCodesSection){
				List<ValueSet.ConceptReferenceComponent> conceptsSoFar = new ArrayList<>();

				ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent();
				concept.setCode(currentRow.getCell(0).getStringCellValue());
				concept.setDisplay(currentRow.getCell(1).getStringCellValue());

				if(codeSystemToCodes.containsKey(currentRow.getCell(2).getStringCellValue())){
					conceptsSoFar = codeSystemToCodes.get(currentRow.getCell(2).getStringCellValue());
				}
				conceptsSoFar.add(concept);
				codeSystemToCodes.put(currentRow.getCell(2).getStringCellValue(), conceptsSoFar);
			}
			if(currentRow.getCell(0) != null && currentRow.getCell(0).getStringCellValue().toLowerCase().equals("code")){
				inCodesSection = true;
			}
		}
		for(Map.Entry<String, List<ValueSet.ConceptReferenceComponent>> singleInclude : codeSystemToCodes.entrySet()){
			ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
			component.setSystem(singleInclude.getKey());
			component.setConcept(singleInclude.getValue());
			compose.addInclude(component);
		}
		valueSet.setCompose(compose);
		ValueSetArtifact artifact = new ValueSetArtifact();
		artifact.name = valueSet.getName();
		artifact.resource = valueSet;
		artifact.url = valueSet.getUrl();
		return artifact;
	}

	static void runWithArgs(String[] args, PrintStream out) throws IOException {
		ValueSetImporterArguments arguments = new ValueSetImporterArguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("value-set-importer").console(console).addObject(arguments)
				.build();
		jc.parse(args);

		if (arguments.isDisplayHelp) {
			jc.usage();
		} else {
			FhirContext fhirContext = FhirContext.forR4();

			ObjectMapper om = new ObjectMapper();
			FhirServerConfig config = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
			IGenericClient client = FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
					.createFhirClient(config);


			VSACValueSetImporter importer = new VSACValueSetImporter(client);
			List<ValueSetArtifact> valueSetArtifacts = Lists.newArrayList();
			for (String arg : arguments.spreadsheets) {
				try (InputStream is = new FileInputStream(arg)) {
					valueSetArtifacts.add(importer.createArtifact(is));
				}
			}
			importer.importArtifacts(valueSetArtifacts);
		}
	}

	public static void main(String[] args) throws Exception {
		VSACValueSetImporter.runWithArgs(args, System.out);
	}
}
