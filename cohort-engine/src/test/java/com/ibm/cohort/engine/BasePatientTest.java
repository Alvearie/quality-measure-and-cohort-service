/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.engine.translation.CqlTranslationProvider;
import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;
import com.ibm.cohort.fhir.client.config.IBMFhirServerConfig;

public class BasePatientTest extends BaseFhirTest {
	protected CqlEngineWrapper setupTestFor(Patient patient, String... resources) {
		IBMFhirServerConfig fhirConfig = new IBMFhirServerConfig();
		fhirConfig.setEndpoint("http://localhost:" + HTTP_PORT);
		fhirConfig.setUser("fhiruser");
		fhirConfig.setPassword("change-password");
		fhirConfig.setTenantId("default");

		return setupTestFor(patient, fhirConfig, resources);
	}

	protected CqlEngineWrapper setupTestFor(Patient patient, FhirServerConfig fhirConfig, String... resources) {

		mockFhirResourceRetrieval("/metadata", getCapabilityStatement());
		mockFhirResourceRetrieval(patient);

		CqlEngineWrapper wrapper = new CqlEngineWrapper();
		if (resources != null) {
			/*
			 * Do some hacking to make the pre-existing test resources still function 
			 * with the updated design.
			 */
			FilenameToVersionedIdentifierStrategy strategy = new DefaultFilenameToVersionedIdentifierStrategy() {
				@Override
				public VersionedIdentifier filenameToVersionedIdentifier(String filename) {
					VersionedIdentifier result = null;
					String basename = FilenameUtils.getBaseName(filename);
					if( basename.startsWith("test") ) {
						result = new VersionedIdentifier().withId("Test").withVersion("1.0.0");
					} else { 
						result = super.filenameToVersionedIdentifier( filename );
					}
					return result;
				}
			};
			
			MultiFormatLibrarySourceProvider sourceProvider = new TestClasspathLibrarySourceProvider(
					Arrays.asList(resources),
					strategy);
			CqlTranslationProvider translationProvider = new InJVMCqlTranslationProvider(sourceProvider);

			LibraryLoader libraryLoader = new TranslatingLibraryLoader(sourceProvider, translationProvider);
			wrapper.setLibraryLoader(libraryLoader);
		}

		wrapper.setDataServerConnectionProperties(fhirConfig);
		wrapper.setTerminologyServerConnectionProperties(fhirConfig);
		wrapper.setMeasureServerConnectionProperties(fhirConfig);
		return wrapper;
	}
}
