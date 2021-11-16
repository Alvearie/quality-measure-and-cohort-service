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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * This class is used to read ValueSet definitions from the local filesystem or S3 compatible endpoint.
 * ValueSet definitions are expected to be stored in FHIR xml or JSON format and can be generated from
 * spreadsheets using the com.ibm.cohort.tooling.fhir.ValueSetImporter tool.
 * 
 * In order to use ValueSets stored on the S3 compatible endpoint, the following spark config parameters must be provided:
 * -Dspark.hadoop.fs.s3a.access.key = access_key_value
 * -Dspark.hadoop.fs.s3a.secret.key = secret_key_value
 * -Dspark.hadoop.fs.s3a.endpoint = object store endpoint
 * 
 * If the spark properties are not enabled, the code will fall back on using the
 * following environment variables:
 * AWS_ACCESS_KEY_ID = access_key_value
 * AWS_SECRET_ACCESS_KEY = secret_key_value
 *
 */
public class R4FileSystemFhirTerminologyProvider implements CqlTerminologyProvider {
	private Path terminologyDirectory;
	private Configuration configuration;
	
	private static final Logger LOG = LoggerFactory.getLogger(R4FileSystemFhirTerminologyProvider.class);
	
	private static final FhirContext fhirContext = FhirContext.forR4();
	
	private Map<VersionedIdentifier, Map<String, Set<String>>> valueSetToCodesCache = new HashMap<>();
	private Map<VersionedIdentifier, List<Code>> valueSetCodeCache = new HashMap<>();
	
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
		LOG.debug("Entry: in() ValueSet.getId=[{}] version=[{}]", valueSetInfo.getId(), valueSetInfo.getVersion());
		
		loadFromFile(valueSetInfo);		
		VersionedIdentifier valueSetIdentifier = createVersionedIdentifierForValueSet(valueSetInfo);
		
		//get the valueSet codes from the cache if it is there
		Map<String, Set<String>> codesToCodeSystems = valueSetToCodesCache.get(valueSetIdentifier);
		if(codesToCodeSystems != null) {
			Set<String> systems = codesToCodeSystems.get(code.getCode());
			
			if(systems != null && !systems.isEmpty()) {
				//per the cql spec https://cql.hl7.org/09-b-cqlreference.html#in-valueset, if there
				//are codes with more than 1 codesystem present in the valueset, throw an error
				if( systems.size() > 1) {
					throw new IllegalArgumentException("Ambiguous code lookup of code[" + code.getCode()+ "] under valueset[" + valueSetIdentifier.getId()+"]"); 
				} else if( (code.getSystem() != null && systems.contains(code.getSystem())) ||  
							code.getSystem() == null ){
					return true;
				}
			}
		}
		

		LOG.debug("Exit: in() ValueSet.getId=[{}] version=[{}]", valueSetInfo.getId(), valueSetInfo.getVersion());
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
		LOG.debug("Entry: expand() ValueSet.getId=[{}] version=[{}]", valueSetInfo.getId(), valueSetInfo.getVersion());		
		
		loadFromFile(valueSetInfo);
		VersionedIdentifier valueSetIdentifier = createVersionedIdentifierForValueSet(valueSetInfo);
		List<Code> codes = valueSetCodeCache.get(valueSetIdentifier);

		LOG.debug("Exit: expand() ValueSet.getId=[{}] version=[{}] found {} codes", valueSetInfo.getId(), valueSetInfo.getVersion(), codes.size());
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
	 */
	protected void loadFromFile(ValueSetInfo valueSetInfo) throws RuntimeException {
		LOG.debug("Entry: loadFromFile() ValueSet.getId=[{}] version=[{}]", valueSetInfo.getId(), valueSetInfo.getVersion());
		
		VersionedIdentifier valueSetIdentifier = createVersionedIdentifierForValueSet(valueSetInfo);
		String valueSetId = valueSetIdentifier.getId();
		
		//get the valueSet codes from the cache if it is there
		Map<String, Set<String>> codesToCodeSystems = valueSetToCodesCache.get(valueSetIdentifier);
		List<Code> codeList = valueSetCodeCache.get(valueSetIdentifier);
		if (codesToCodeSystems == null || codeList == null) {
			LOG.debug("loadFromFile() valueSetId={} not found in cache, attempting to load from file", valueSetId);
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
					LOG.warn(
							"Multiple ValueSet files found for ValueSet {} in terminology directory {}. File {} will be used.",
							valueSetId, terminologyDirectory.toString(), valueSetFiles[0].toString());
				}

				ValueSet valueSetFhirR4 = null;
				try {
					//Use the fhir parsers to convert file contents back into ValueSet fhir object
					if (valueSetFiles[0].getPath().getName().toLowerCase().endsWith(".xml")) {
						valueSetFhirR4 = (ValueSet) fhirContext.newXmlParser()
								.parseResource(new InputStreamReader(fileSystem.open(valueSetFiles[0].getPath())));
						LOG.info("Unmarshalled xml {}", valueSetFhirR4.getId());
					} else if (valueSetFiles[0].getPath().getName().toLowerCase().endsWith(".json")) {
						valueSetFhirR4 = (ValueSet) fhirContext.newJsonParser()
								.parseResource(new InputStreamReader(fileSystem.open(valueSetFiles[0].getPath())));
						LOG.info("Unmarshalled json {}", valueSetFhirR4.getId());
					}
					
					//Add the codes to a HashSet using a wrapper object with the correct hashCode/equals behavior
					//This improves performance for the in() method for code lookup in large valuesets
					codesToCodeSystems = new HashMap<String, Set<String>>();
					//cache the list of code objects for the expand method
					codeList = new ArrayList<Code>();
					for (ConceptSetComponent csc : valueSetFhirR4.getCompose().getInclude()) {
						for (ConceptReferenceComponent cfc : csc.getConcept()) {
							codeList.add(new Code().withCode(cfc.getCode()).withDisplay(cfc.getDisplay()).withSystem(csc.getSystem()).withVersion(csc.getVersion()));
							Set<String> codeSystems = codesToCodeSystems.get(cfc.getCode());
							if(codeSystems == null) {
								codeSystems = new HashSet<String>();
								codesToCodeSystems.put(cfc.getCode(), codeSystems);
							}
							codeSystems.add(csc.getSystem());
						}
					}

					valueSetToCodesCache.put(valueSetIdentifier, codesToCodeSystems);
					valueSetCodeCache.put(valueSetIdentifier, codeList);
				} catch (ConfigurationException | DataFormatException | IOException e) {
					LOG.error("Error attempting to deserialize ValueSet "+ valueSetFiles[0].getPath().toString(), e);
					throw new RuntimeException("Error attempting to deserialize ValueSet "+ valueSetFiles[0].getPath().toString(), e);
				}
			}
		}
		
		LOG.debug("Exit: loadFromFile() ValueSet.getId=[{}] version=[{}]", valueSetInfo.getId(), valueSetInfo.getVersion());
	}
	
	//convenience method to create a hashmap key for a valueset
	protected VersionedIdentifier createVersionedIdentifierForValueSet(ValueSetInfo valueSetInfo) {
		String valueSetId;
		
		//strip of the urn or url portions of the id if they exist
		if (valueSetInfo.getId().startsWith("urn:oid:")) {
			valueSetId = valueSetInfo.getId().replace("urn:oid:", "");
		} else if (valueSetInfo.getId().startsWith("http")) {
			valueSetId = valueSetInfo.getId().substring(valueSetInfo.getId().lastIndexOf("/")+1);
		} else {
			valueSetId = valueSetInfo.getId();
		}
		
		LOG.debug("createVersionedIdentifierForValueSet() trimmed valueSetId={}", valueSetId);
		
		String valueSetVersion = valueSetInfo.getVersion();	
		VersionedIdentifier valueSetIdentifier = new VersionedIdentifier().withId(valueSetId)
				.withVersion(valueSetVersion);
		
		return valueSetIdentifier;
	}
}