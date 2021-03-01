/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.helpers;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.MetadataResource;

/**
 * Helper methods for dealing with FHIR canonical URL syntax. <a href=
 * "https://www.hl7.org/fhir/references.html#canonical">https://www.hl7.org/fhir/references.html#canonical</a>
 */
public class CanonicalHelper {
	/**
	 * A canonical URL in FHIR consists of a url + an optional version where the
	 * fields are delimited by a pipe character ('|'). This method will remove the
	 * optional version if present.
	 * 
	 * @param canonicalUrl FHIR canonical URL
	 * @return URL without the version information
	 */
	public static String removeVersion(String canonicalUrl) {
		Pair<String, String> parts = separateParts(canonicalUrl);
		return parts.getLeft();
	}

	/**
	 * A canonical URL in FHIR consists of a url + an optional version where the
	 * fields are delimited by a pipe character ('|'). This method will separate the
	 * parts.
	 * 
	 * @param canonicalUrl
	 * @return a pair of values where the left side is the url and the right side is
	 *         the version.
	 */
	public static Pair<String, String> separateParts(String canonicalUrl) {
		String url = canonicalUrl;
		String version = null;

		int pos = canonicalUrl.lastIndexOf('|');
		if (pos != -1) {
			url = canonicalUrl.substring(0, pos);
			version = canonicalUrl.substring(pos + 1);
		}

		return Pair.of(url, version);
	}

	public static String toCanonicalUrl(MetadataResource resource) {
		return String.format("%s|%s", resource.getUrl(), resource.getVersion());
	}
}
