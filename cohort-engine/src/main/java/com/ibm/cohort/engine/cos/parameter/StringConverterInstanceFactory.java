/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos.parameter;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.converters.BooleanConverter;
import com.beust.jcommander.converters.IntegerConverter;
import com.beust.jcommander.converters.StringConverter;

public class StringConverterInstanceFactory {
	private static final Map<Class<?>, IStringConverter<?>> converters;

	private static final StringConverter stringConverter = new StringConverter();
	private static final IntegerConverter integerConverter = new IntegerConverter("Dynamic Parameter Converter for ints");
	private static final BooleanConverter booleanConverter = new BooleanConverter("Dynamic Parameter Converter for bools");

	static {
		converters = new HashMap<>();
		converters.put(String.class, stringConverter);
		converters.put(Integer.class, integerConverter);
		converters.put(int.class, integerConverter);
		converters.put(boolean.class, booleanConverter);
		converters.put(Boolean.class, booleanConverter);
	}

	private StringConverterInstanceFactory() {
	}

	public static IStringConverter<?> getConverter(Class<?> forType) {
		return converters.get(forType);
	}
}
