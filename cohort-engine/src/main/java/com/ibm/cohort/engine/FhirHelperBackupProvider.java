/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.hl7.elm.r1.VersionedIdentifier;

public class FhirHelperBackupProvider {
	private static VersionedIdentifier fhirHelpersVersion = new VersionedIdentifier().withId("FHIRHelpers");
	private static final String FHIR_HELPER_DEFAULT_VERSION = "4.0.0";

	FhirHelperBackupProvider(String fhirVersion){
		fhirHelpersVersion.setVersion(fhirVersion);
	}

	public static void addClasspathFhirHelpers(Map<VersionedIdentifier, Map<LibraryFormat, InputStream>> sources){
		Map<LibraryFormat, InputStream> specFormat = sources.computeIfAbsent(fhirHelpersVersion, key -> new HashMap<>());
		if(specFormat.isEmpty()) {
			InputStream fhirHelperResource = ClasspathLibrarySourceProvider.class.getResourceAsStream(String.format("/org/hl7/fhir/%s-%s.xml", fhirHelpersVersion.getId(), FHIR_HELPER_DEFAULT_VERSION));
			specFormat.put(LibraryFormat.XML, fhirHelperResource);
		}
	}
}
