/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import java.security.MessageDigest;
import java.util.Base64;

import org.hl7.fhir.r4.model.MetadataResource;

/**
 * In order to guarantee uniqueness when PUT'ing the same resource to the
 * same FHIR server simultaneously in separate threads, a Resource.id 
 * value needs to be specified. The IBM FHIR server will not guarantee 
 * uniqueness when using search params (e.g. name:exact=XXX&amp;version=YYY).
 * 
 * In order to keep the logical name + version key as the unique identifier
 * across environments outside of FHIR, we decided to use an algorithm to
 * generate a unique resource ID inside the FHIR server. Resource.id has
 * a 64 character limit and is restricted to only letters, numbers, periods,
 * and dashes. 
 * 
 * Using SHA-256 hashing we can get a near unique key and then we can use
 * Base64 encoding to get that close to the FHIR id constraints. Further
 * manipulation is done to change the Base64 encoding special characters (+/)
 * into allowed special characters (-.) and to trim the padding.
 * 
 * This algorithm generates a one-way encoding and is never intended to be
 * decoded back to the original value.
 */
public class SHA256NameVersionIdStrategy implements IdStrategy {

	@Override
	public String generateId(MetadataResource resource) throws Exception {
		String name = resource.getName();
		String version = resource.getVersion();
		
		String unencoded = name;
		if( version != null ) {
			unencoded = name + "-" + version;
		};
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte [] binaryData = digest.digest( unencoded.getBytes("UTF-8") );
		String b64encoded = Base64.getEncoder().withoutPadding().encodeToString(binaryData);
		String madeValidForFHIR = b64encoded.replace("+", "-").replace("/", ".");
		return madeValidForFHIR;
	}

}
