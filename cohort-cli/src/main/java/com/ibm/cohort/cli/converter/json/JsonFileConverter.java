/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cli.converter.json;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileConverter<T extends Object> extends BaseConverter<T> {

	private final Class<T> clazz;

	public JsonFileConverter(String optionName, Class<T> clazz) {
		super(optionName);
		this.clazz = clazz;
	}

	@Override
	public T convert(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			File jsonFile = new File(value);
			return objectMapper.readValue(jsonFile, clazz);
		} catch (IOException e) {
			throw new ParameterException(getErrorString(value, clazz.getSimpleName()), e);
		}
	}
}
