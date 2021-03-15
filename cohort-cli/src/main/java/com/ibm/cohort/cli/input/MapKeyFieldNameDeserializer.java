/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * Deserialize a Map key that was written out as its String value instead of
 * "key" : "value". This makes the JSON that is generated more aesthetically
 * pleasing and slightly easier for a human.
 */
public class MapKeyFieldNameDeserializer extends KeyDeserializer {

	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
		return key;
	}
}
