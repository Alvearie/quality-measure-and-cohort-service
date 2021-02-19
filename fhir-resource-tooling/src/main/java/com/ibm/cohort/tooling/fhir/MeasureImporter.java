/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.ResourceType;

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

/**
 * Load a FHIR server with resources provided in the UI measure export format. Resources are cross-referenced
 * with existing content in the FHIR server by canonical url. Resources that already exist are updated as needed.
 * 
 * Pre-reqs:
 * $&lt; keytool -importkeystore -srckeystore /path/to/git/FHIR/fhir-server/liberty-config/resources/security/fhirKeyStore.p12 -srcstorepass change-password -cacerts -deststorepass changeit
 */
public class MeasureImporter {
	private static final String DEPLOYPACKAGE = "deploypackage/";
	private static final String JSON_EXT = ".json";

	/**
	 * General metadata about a deployable artifact.
	 */
	public abstract static class ArtifactMetadata {
		public String resourceType;
		public MetadataResource resource;
		public String url;
		public String version;
		public String name;
		public String id;
		
		private ArtifactMetadata() {
		}

		public void fromResource(MetadataResource metadata) {
			this.resourceType = metadata.fhirType();
			this.resource = metadata;
			this.url = metadata.getUrl();
			this.version = metadata.getVersion();
			this.name = metadata.getName();
			this.id = metadata.getId();
		}

		public abstract int updateDependencies(List<LibraryArtifact> libraries);
	}

	/**
	 * Metadata about a library artifact.
	 */
	public static class LibraryArtifact extends ArtifactMetadata {

		private LibraryArtifact() {
		}
		
		@Override
		public int updateDependencies(List<LibraryArtifact> libraries) {
			int numUpdated = 0;

			List<RelatedArtifact> relatedArtifacts = ((Library) resource).getRelatedArtifact();
			if (relatedArtifacts != null) {
				for (RelatedArtifact related : relatedArtifacts) {
					if (related.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON && related.getResource().startsWith("Library/")) {
						for (LibraryArtifact library : libraries) {
							if (related.getResource().equals(library.url)) {
								related.setResource("Library/" + library.id);
								numUpdated++;
								break;
							}
						}

					}
				}
			}

			return numUpdated;
		}
	}

	/**
	 * Metadata about a measure artifact.
	 */
	public static class MeasureArtifact extends ArtifactMetadata {

		private MeasureArtifact() {
		}
		
		@Override
		public int updateDependencies(List<LibraryArtifact> libraries) {
			int numUpdated = 0;

			for (CanonicalType canonical : ((Measure) resource).getLibrary()) {
				for (LibraryArtifact library : libraries) {
					if (canonical.getValueAsString().equals(library.url)) {
						canonical.setValue("Library/" + library.id);
						numUpdated++;
						break;
					}
				}
			}

			return numUpdated;
		}
	}

	/**
	 * Wrapper for command-line arguments.
	 */
	public static final class Arguments {
		@Parameter(names = { "-m",
				"--measure-server" }, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.", required = true)
		File measureServerConfigFile;

		@Parameter(names = { "-h", "--help" }, description = "Show this help", help = true)
		boolean isDisplayHelp;

		@Parameter(description = "<zip> [<zip> ...]", required = true)
		List<String> artifactPaths;
	}

	private IGenericClient client;
	private IParser parser;

	public MeasureImporter(IGenericClient client) {
		this.client = client;
		this.parser = client.getFhirContext().newJsonParser();
	}

	/**
	 * Import measure artifacts from UI export bundle.
	 * @param is InputStream containing artifacts to import
	 * @throws Exception any error
	 */
	public void importArtifacts(InputStream is) throws Exception {
		List<LibraryArtifact> libraries = new ArrayList<LibraryArtifact>();
		MeasureArtifact measure = new MeasureArtifact();

		// read the ZIP contents, read contents into memory, and index some metadata
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (isDeployable(entry)) {
				ArtifactMetadata artifact = indexArtifact(zis);
				if (artifact instanceof LibraryArtifact) {
					libraries.add((LibraryArtifact) artifact);
				} else {
					measure = (MeasureArtifact) artifact;
				}
			}
		}

		// find or create the base library resources
		for (LibraryArtifact library : libraries) {
			Bundle bundle = client.search().forResource(Library.class).where(Library.URL.matches().value(library.url))
					.returnBundle(Bundle.class).execute();
			if( bundle.getEntry().size() > 0 ) {
				library.id = bundle.getEntryFirstRep().getResource().getIdElement().getIdPart();
				library.resource.setId( library.id );
			} else { 
				MethodOutcome outcome = client.create().resource( parser.encodeResourceToString(library.resource) ).execute();
				if( outcome.getCreated() ) {
					library.id = outcome.getId().getIdPart();
					library.resource.setId( library.id ); 
				}
			}
		}

		// update the libraries depends-on links with the IDs of each of the created/found libraries
		for (LibraryArtifact depender : libraries) {
			int numUpdated = depender.updateDependencies(libraries);
			if (numUpdated > 0) {
				client.update().resource( parser.encodeResourceToString(depender.resource) ).execute();
			}
		}

		// find or create the measure resource
		Bundle bundle = client.search().forResource(Measure.class).where(Measure.URL.matches().value(measure.url))
				.returnBundle(Bundle.class).execute();
		if( bundle.hasEntry() ) {
			measure.id = bundle.getEntryFirstRep().getResource().getIdElement().getIdPart();
			measure.resource.setId( measure.id );
		} else {
			MethodOutcome outcome = client.create().resource( parser.encodeResourceToString(measure.resource) ).execute();
			if( outcome.getCreated() ) {
				measure.id = outcome.getId().getIdPart();
				measure.resource.setId( measure.id );
			}
		}
		
		// update the measure's primary library link
		int numUpdated = measure.updateDependencies(libraries);
		if (numUpdated == 0) {
			throw new Exception("Failed to update measure Library reference(s). No matching libraries found.");
		} else {
			client.update().resource( parser.encodeResourceToString(measure.resource) ).execute();
		}
	}

	/**
	 * Check whether ZipEntry represents a resource that should be deployed to the FHIR server
	 * @param entry metadata about the zip file entry
	 * @return true if entry is deployable or false otherwise
	 */
	public static boolean isDeployable(ZipEntry entry) {
		return entry.getName().startsWith(DEPLOYPACKAGE) && entry.getName().endsWith(JSON_EXT);
	}

	protected ArtifactMetadata indexArtifact(InputStream is) {
		ArtifactMetadata artifact;

		IBaseResource resource = parser.parseResource(is);
		if (resource instanceof MetadataResource) {
			MetadataResource metadata = (MetadataResource) resource;

			if (metadata.getResourceType().equals(ResourceType.Library)) {
				artifact = new LibraryArtifact();
			} else if (metadata.getResourceType().equals(ResourceType.Measure)) {
				artifact = new MeasureArtifact();
			} else {
				throw new IllegalArgumentException("Invalid resource type " + resource.getClass().getSimpleName());
			}

			artifact.fromResource(metadata);

		} else {
			throw new IllegalArgumentException("Invalid resource type " + resource.getClass().getSimpleName());
		}

		return artifact;
	}

	/**
	 * Execute measure import process with given arguments. Console logging is redirected to the 
	 * provided stream.
	 * @param args Program arguments
	 * @param out Sink for console output
	 * @throws Exception on any error.
	 */
	public static void runWithArgs(String[] args, PrintStream out) throws Exception {
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("measure-importer").console(console).addObject(arguments)
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

			MeasureImporter importer = new MeasureImporter(client);
			for (String arg : arguments.artifactPaths) {
				try (InputStream is = new FileInputStream(arg)) {
					importer.importArtifacts(is);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		MeasureImporter.runWithArgs(args, System.out);
	}
}
