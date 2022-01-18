/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.tooling.s3.S3Configuration;
import com.ibm.cohort.valueset.ValueSetArtifact;
import com.ibm.cohort.valueset.ValueSetUtil;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;


public class ValueSetImporter {
	private static final Logger logger = LoggerFactory.getLogger(ValueSetImporter.class.getName());
	
	private enum FileFormat {JSON, XML};
	private enum OutputLocations{NONE, LOCAL, S3, BOTH}
	
	public static final class ValueSetImporterArguments {
		@Parameter(names = {"-m",
				"--measure-server"}, description = "Path to JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources.")
		File measureServerConfigFile;

		@Parameter(names = {"--override-existing-value-sets"}, description = "Will force insertion of valuesets, even if they already exist")
		boolean overrideValueSets;

		@Parameter(names = {"-c", "--code-system-mappings"}, description = "Custom code system mappings")
		String filename;

		@Parameter(names = {"-h", "--help"}, description = "Show this help", help = true)
		boolean isDisplayHelp;

		@Parameter(names = {"--output-locations"}, description = "If any but \"NONE\" is specified, value sets will be converted to FHIR format and written to a location specified by the bucket or filePath parameters. Value sets will not be imported into the FHIR server if this option is set. Default is NONE.")
		OutputLocations fileOutputLocation = OutputLocations.NONE;

		@Parameter(names = {"-p", "--file-system-output-path"}, description = "Local filesystem path to write results out to (will only be used if output-locations is either BOTH or LOCAL.")
		String fileSystemOutputPath;

		@Parameter(names = {"-b", "--bucket"}, description = "Bucket to write results out to (will only be used if output-locations is either BOTH or S3).")
		String bucket;

		@Parameter(names = {"--S3-configuration"}, description = "a json file containing all the relevant S3 configuration needs for access")
		File S3JsonConfigs;

		@Parameter(names = {"-o", "--file-system-output-format"}, description = "Format to use when exporting value sets to the file system when using the -p/--file-system-output-path parameters. Valid values are JSON or XML. If not specified, the default output format will be JSON", required = false)
		FileFormat filesystemOutputFormat = FileFormat.JSON;

		@Parameter(description = "The list of value set spreadsheets to import", required = true)
		List<String> spreadsheets;
		
		public void validate() {
			//check to make sure we are either exporting to file OR importing to a fhir server, but not both
			if(fileOutputLocation != OutputLocations.NONE && measureServerConfigFile != null) {
				throw new IllegalArgumentException("Parameters [-m, --measure-server] and [--output-locations] cannot both be specified on the same invocation");
			}
			
			if(fileOutputLocation == OutputLocations.NONE && measureServerConfigFile == null) {
				throw new IllegalArgumentException("Either [-m, --measure-server] or [--output-locations] must be specified. Please supply either a valid measure server or a valid (not NONE) output location.");
			}
			if((fileOutputLocation == OutputLocations.BOTH || fileOutputLocation == OutputLocations.S3) && (bucket == null || S3JsonConfigs == null)){
				throw new IllegalArgumentException("Required information for writing to S3 is missing! Please specify both a bucket and the S3 configurations.");
			}
			if((fileOutputLocation == OutputLocations.BOTH || fileOutputLocation == OutputLocations.LOCAL) && fileSystemOutputPath == null){
				throw new IllegalArgumentException("Required information for writing locally is missing! Please specify a file system output path (-p/--file-system-output-path).");
			}
		}
	}

	void runWithArgs(String[] args, PrintStream out) throws IOException {
		ValueSetImporterArguments arguments = new ValueSetImporterArguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("value-set-importer").console(console).addObject(arguments)
				.build();
		jc.parse(args);

		if (arguments.isDisplayHelp) {
			jc.usage();
		} else {
			arguments.validate();
			FhirContext fhirContext = FhirContext.forR4();
			//only connect to fhir server if we are not writing it to file system
			IGenericClient client = null;
			ObjectMapper om = new ObjectMapper();
			if(arguments.fileOutputLocation == OutputLocations.NONE) {
				FhirServerConfig config = om.readValue(arguments.measureServerConfigFile, FhirServerConfig.class);
				client = FhirClientBuilderFactory.newInstance().newFhirClientBuilder(fhirContext)
						.createFhirClient(config);
			}
			Map<String, String> codeSystemMappings = null;
			if(arguments.filename != null) {
				codeSystemMappings = ValueSetUtil.getMapFromInputStream(new FileInputStream(new File(arguments.filename)));
			}

			for (String arg : arguments.spreadsheets) {
				try (InputStream is = new FileInputStream(arg)) {
					ValueSetArtifact artifact = ValueSetUtil.createArtifact(is, codeSystemMappings);
					
					//only import the value set to fhir server if we are not writing the value set to file system
					if(arguments.fileOutputLocation == OutputLocations.NONE) {
						String retVal = ValueSetUtil.importArtifact(client, artifact, arguments.overrideValueSets);
						if(retVal == null){
							logger.error("Value set already exists! Please provide the override option if you would like to override this value set.");
						}
					}else {
						//write value set to file system
						ValueSet vs = artifact.getFhirResource();
						
						//If the valueset id contains urn:oid, remove it to make a valid filename
						String valueSetId = vs.getId().startsWith("urn:oid:") ? vs.getId().replace("urn:oid:", "") : vs.getId();						
						String vsFileName = valueSetId + "." + arguments.filesystemOutputFormat.toString().toLowerCase();

						if(arguments.fileOutputLocation == OutputLocations.BOTH || arguments.fileOutputLocation == OutputLocations.S3){
							S3Configuration S3Config = om.readValue(arguments.S3JsonConfigs, S3Configuration.class);

							AmazonS3 S3Client = createClient(S3Config.getAccess_key_id(), S3Config.getSecret_access_key(), S3Config.getEndpoint(), S3Config.getLocation());
							putToS3(arguments, fhirContext, vs, vsFileName, S3Client);
						}
						if(arguments.fileOutputLocation == OutputLocations.BOTH || arguments.fileOutputLocation == OutputLocations.LOCAL) {
							try (BufferedWriter writer = new BufferedWriter(new FileWriter(arguments.fileSystemOutputPath + System.getProperty("file.separator") + vsFileName))) {
								//create the output dir if it doesn't exist
								File outputDir = new File(arguments.fileSystemOutputPath);
								if (!outputDir.exists()) {
									outputDir.mkdir();
								}

								//write to xml or json format
								if (arguments.filesystemOutputFormat == FileFormat.JSON) {
									fhirContext.newJsonParser().encodeResourceToWriter(vs, writer);
								} else if (arguments.filesystemOutputFormat == FileFormat.XML) {
									fhirContext.newXmlParser().encodeResourceToWriter(vs, writer);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void putToS3(ValueSetImporterArguments arguments, FhirContext fhirContext, ValueSet vs, String vsFileName, AmazonS3 S3Client) {
		ObjectMetadata metadata = new ObjectMetadata();
		byte[] arr = fhirContext.newJsonParser().encodeResourceToString(vs).getBytes();
		metadata.setContentLength(arr.length);
		PutObjectRequest put = new PutObjectRequest(arguments.bucket, vsFileName, new ByteArrayInputStream(arr), metadata);
		S3Client.putObject(put);
	}

	public AmazonS3 createClient(String api_key, String service_instance_id, String endpoint_url, String location)
	{
		AWSCredentials credentials = new BasicAWSCredentials(api_key, service_instance_id);
		ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);
		clientConfig.setUseTcpKeepAlive(true);

		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint_url, location)).withPathStyleAccessEnabled(true)
				.withClientConfiguration(clientConfig).build();
	}

	public static void main(String[] args) throws Exception {
		ValueSetImporter valueSetImporter = new ValueSetImporter();
		valueSetImporter.runWithArgs(args, System.out);
	}
}


