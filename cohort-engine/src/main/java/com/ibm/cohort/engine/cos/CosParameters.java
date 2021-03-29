/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cohort.engine.cos.parameter.FunctionFactory;

public class CosParameters {

	@CosParameter(cliName = "cos.request.timeout", setterMethod = "withRequestTimeout")
	public static int requestTimeout;

	@CosParameter(cliName = "cos.tcp.keep.alive", setterMethod = "withTcpKeepAlive")
	public static boolean keepTcpAlive;

	private static final Map<String, BiFunction<Object, String, Object>> cliLookup;
	static {
		cliLookup = new HashMap<>();

		for (Field field : CosParameters.class.getFields()) {
			CosParameter parameter = field.getAnnotation(CosParameter.class);
			try {
				Method method = ClientConfiguration.class.getMethod(parameter.setterMethod(), field.getType());
				BiFunction<Object, String, Object> setter = FunctionFactory.toFunction(method, field.getType());

				cliLookup.put(parameter.cliName(), setter);

			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static ClientConfiguration clientFrom(Map<String, String> params) {
		ClientConfiguration configuration = new ClientConfiguration();

		for (Map.Entry<String, String> param : params.entrySet()) {
			String key = param.getKey();
			String value = param.getValue();

			BiFunction<Object, String, Object> setter = cliLookup.get(key);
			setter.apply(configuration, value);
		}

		return configuration;
	}
}
