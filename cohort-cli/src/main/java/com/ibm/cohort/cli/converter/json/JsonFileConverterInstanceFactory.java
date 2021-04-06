/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cli.converter.json;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterInstanceFactory;
import com.beust.jcommander.Parameter;

public class JsonFileConverterInstanceFactory implements IStringConverterInstanceFactory {
	@Override
	public IStringConverter<?> getConverterInstance(Parameter parameter, Class<?> forType, String optionName) {
		return new JsonFileConverter<>(optionName, forType);
	}
}
