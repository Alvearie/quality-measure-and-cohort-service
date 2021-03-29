/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos.parameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import com.beust.jcommander.IStringConverter;

public class FunctionFactory {

	private FunctionFactory() {
	}

	public static BiFunction<Object, String, Object> toFunction(Method method, Class<?> clazz) {
		return (obj, arg) -> {
			try {
				IStringConverter<?> converter = StringConverterInstanceFactory.getConverter(clazz);

				return method.invoke(obj, converter.convert(arg));
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
