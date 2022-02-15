/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.helpers;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Helper methods for dealing with FHIR canonical URL syntax. <a href=
 * "https://www.hl7.org/fhir/references.html#canonical">https://www.hl7.org/fhir/references.html#canonical</a>
 */
public class CanonicalHelper {
	/**
	 * A canonical URL in FHIR consists of a url + an optional version where the
	 * fields are delimited by a pipe character ('|'). This method will separate the
	 * parts.
	 * 
	 * @param canonicalUrl canonical URL value
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

	public static String toCanonicalUrl(String url, String version) {
		return url + "|" + version;
	}
}
