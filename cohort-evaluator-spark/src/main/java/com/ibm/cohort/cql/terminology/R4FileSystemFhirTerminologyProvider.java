/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.terminology;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

/**
 * This class is used to read ValueSet definitions from the local filesystem or S3 compatible source.
 * ValueSet definitions are expected to be stored in FHIR xml or JSON format and can be generated from
 * spreadsheets using the com.ibm.cohort.tooling.fhir.ValueSetImporter tool.
 * 
 * In order to use ValueSets stored on the filesystem, the following spark config parameters must be provided:
 * -Dspark.hadoop.fs.s3a.access.key = access_key_value
 * -Dspark.hadoop.fs.s3a.secret.key = secret_key_value
 * -Dspark.hadoop.fs.s3a.endpoint = object store endpoint
 * 
 * as well as setting the following environment variables:
 * AWS_ACCESS_KEY = access_key_value
 * AWS_ENDPOINT = object store endpoint
 * AWS_LOCATION = location value ie us-east
 * AWS_SECRET_ACCESS_KEY = secret_key_value
 *
 */
public class R4FileSystemFhirTerminologyProvider implements CqlTerminologyProvider {
	private Path terminologyDirectory;
	private Configuration configuration;
	
	private static final Logger LOG = LoggerFactory.getLogger(R4FileSystemFhirTerminologyProvider.class);
	
	private static final FhirContext fhirContext = FhirContext.forR4();
	
	private Map<VersionedIdentifier, ValueSet> valueSetCache = new HashMap<>();
	
	public R4FileSystemFhirTerminologyProvider(Path terminologyDirectory, Configuration configuration) {
		super();
		this.terminologyDirectory = terminologyDirectory;
		LOG.info("TerminologyDirectory is " + terminologyDirectory.toString());
		this.configuration = configuration;
	}

	/* (non-Javadoc)
	 * 
	 * Returns true if the provided code exists in the provided ValueSet
	 * 
	 * @see org.opencds.cqf.cql.engine.terminology.TerminologyProvider#in(org.opencds.cqf.cql.engine.runtime.Code, org.opencds.cqf.cql.engine.terminology.ValueSetInfo)
	 */
	@Override
	public boolean in(Code code, ValueSetInfo valueSetInfo) {
		LOG.debug("Entry: in() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
		ValueSet valueSetFhirR4 = loadFromFile(valueSetInfo);

		// Check and see if the input code is in the ValueSet
		if (valueSetFhirR4 != null) {
			for (ConceptSetComponent csc : valueSetFhirR4.getCompose().getInclude()) {
				for (ConceptReferenceComponent cfc : csc.getConcept()) {
					// tabishop is equivalent ok here, or do we want equals (ie code system version needs to match?)
					// there was test case for null code system so checking for that
					if ( ( code.getSystem() == null || (code.getSystem() != null && csc.getSystem().equalsIgnoreCase(code.getSystem())))
							&& code.getCode().equalsIgnoreCase(cfc.getCode())) {
						LOG.debug("Exit: in() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
						return true;
					}
				}
			}
		}

		LOG.debug("Exit: in() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
		return false;
	}
	
	

	/* (non-Javadoc)
	 * 
	 * Returns the list of Codes in the given ValueSet
	 * 
	 * @see org.opencds.cqf.cql.engine.terminology.TerminologyProvider#expand(org.opencds.cqf.cql.engine.terminology.ValueSetInfo)
	 */
	@Override
	public Iterable<Code> expand(ValueSetInfo valueSetInfo) {
		LOG.debug("Entry: expand() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
		ValueSet valueSetFhirR4 = loadFromFile(valueSetInfo);
		List<Code> codes = new ArrayList<>();

		// Check and see if the input code is in the ValuSet
		if (valueSetFhirR4 != null) {
			for (ConceptSetComponent csc : valueSetFhirR4.getCompose().getInclude()) {
				for (ConceptReferenceComponent cfc : csc.getConcept()) {

					Code code = new Code().withCode(cfc.getCode()).withDisplay(cfc.getDisplay())
							.withSystem(csc.getSystem()).withVersion(csc.getVersion());

					codes.add(code);
				}
			}
		}

		LOG.debug("Exit: expand() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"] found " + codes.size() + " codes");
		return codes;
	}

	/* (non-Javadoc)
	 * 
	 * Return a Code object containing all available information populated from the codeSystem
	 * 
	 * @see org.opencds.cqf.cql.engine.terminology.TerminologyProvider#lookup(org.opencds.cqf.cql.engine.runtime.Code, org.opencds.cqf.cql.engine.terminology.CodeSystemInfo)
	 */
	@Override
	public Code lookup(Code code, CodeSystemInfo codeSystem) {
		/* Team decided not to implement this method at this time as our current users are
		 * not going to be using complex external codesystems. In the future, if we wanted
		 * to implement this, we could consider using the IBM KnowledgeMap here
		 */
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Loads ValueSet definitions from the filesystem or S3 compatible location
	 * ValueSet definitions are expected to be stored in FHIR xml or JSON format
	 * named using the valueSet id (ie 2.16.840.1.113762.1.4.1114.7.json)
	 * 
	 * @param valueSetInfo contains information for teh VlaueSet we want to load
	 * @return A ValueSet object populated from the file definition or null if no valueset file was found
	 */
	protected ValueSet loadFromFile(ValueSetInfo valueSetInfo) throws RuntimeException {
		LOG.debug("Entry: loadFromFile() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
		String valueSetId;
		
		//strip of the urn or url portions of the id if they exist
		if (valueSetInfo.getId().startsWith("urn:oid:")) {
			valueSetId = valueSetInfo.getId().replace("urn:oid:", "");
		} else if(valueSetInfo.getId().startsWith("http") || valueSetInfo.getId().startsWith("https") ) {
			valueSetId = valueSetInfo.getId().substring(valueSetInfo.getId().lastIndexOf("/")+1);
		} else {
			valueSetId = valueSetInfo.getId();
		}
		
		LOG.debug("loadFromFile() trimmed valueSetId=" + valueSetId);
		
		String valueSetVersion = valueSetInfo.getVersion();	
		VersionedIdentifier valueSetIdentifier = new VersionedIdentifier().withId(valueSetId)
				.withVersion(valueSetVersion);
		
		//get the value from the cache if it is there
		ValueSet valueSetFhirR4 = valueSetCache.get(valueSetIdentifier);
		if (valueSetFhirR4 == null) {
			LOG.debug("loadFromFile() valueSetId=" + valueSetId + " not found in cache, attempting to load from file");
			FileStatus[] valueSetFiles;
			FileSystem fileSystem;
			
			//List the files in the terminology directory that end in xml or json
			try {
				fileSystem = terminologyDirectory.getFileSystem(configuration);
				valueSetFiles = fileSystem.listStatus(terminologyDirectory, new PathFilter() {
					@Override
					public boolean accept(Path path) {
						return path.getName().equalsIgnoreCase(valueSetId + ".json") || path.getName().equalsIgnoreCase(valueSetId + ".xml");
					}
				});
			} catch (ConfigurationException | DataFormatException | IOException e) {
				LOG.error("Error attempting to get ValueSet file for ValueSet [" + valueSetId + " from "+ terminologyDirectory.toString(), e);
				throw new RuntimeException("Error attempting to get ValueSet file for ValueSet [" + valueSetId + " from "+ terminologyDirectory.toString(), e);
			}

			if (valueSetFiles.length == 0) {
				LOG.error("No valueSet file " + valueSetId + ".json or " + valueSetId
						+ ".xml found in terminology directory " + terminologyDirectory.toString());
				throw new RuntimeException("No valueSet file " + valueSetId + ".json or " + valueSetId
						+ ".xml found in terminology directory " + terminologyDirectory.toString());
			} else {
				if (valueSetFiles.length > 1) {
					LOG.warn("Multiple ValueSet files found for ValueSet " + valueSetId + " in terminology directory " + terminologyDirectory.toString()
							+ ". File " + valueSetFiles[0].toString() + " will be used.");
				}

				try {
					//Use the fhir parsers to convert file contents back into ValueSet fhir object
					if (valueSetFiles[0].getPath().getName().toLowerCase().endsWith(".xml")) {
						valueSetFhirR4 = (ValueSet) fhirContext.newXmlParser()
								.parseResource(new InputStreamReader(fileSystem.open(valueSetFiles[0].getPath())));
						LOG.info("Unmarshalled xml " + valueSetFhirR4.getId());
					} else if (valueSetFiles[0].getPath().getName().toLowerCase().endsWith(".json")) {
						valueSetFhirR4 = (ValueSet) fhirContext.newJsonParser()
								.parseResource(new InputStreamReader(fileSystem.open(valueSetFiles[0].getPath())));
						LOG.info("Unmarshalled json " + valueSetFhirR4.getId());
					}

					valueSetCache.put(valueSetIdentifier, valueSetFhirR4);
				} catch (ConfigurationException | DataFormatException | IOException e) {
					LOG.error("Error attempting to deserialize ValueSet "+ valueSetFiles[0].getPath().toString(), e);
					throw new RuntimeException("Error attempting to deserialize ValueSet "+ valueSetFiles[0].getPath().toString(), e);
				}
			}
		}
		
		LOG.debug("Exit: loadFromFile() ValueSet.getId=[" + valueSetInfo.getId() + "] version=[" + valueSetInfo.getVersion() +"]");
		return valueSetFhirR4;
	}
}