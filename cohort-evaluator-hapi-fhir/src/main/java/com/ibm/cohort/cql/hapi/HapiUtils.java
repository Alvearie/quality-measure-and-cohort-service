/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.hapi;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;

/**
 * General helper functions and constants for interacting with the HAPI FHIR API.
 */
public class HapiUtils {

    public static boolean canParseFile(String filename, IParser parser) {
        EncodingEnum encoding = parser.getEncoding();
        String expectedSuffix = "." + encoding.getFormatContentType();
        return filename.toLowerCase().endsWith(expectedSuffix);
    }

}
