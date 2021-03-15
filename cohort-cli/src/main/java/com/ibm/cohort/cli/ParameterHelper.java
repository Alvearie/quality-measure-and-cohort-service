/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.cohort.engine.parameter.BooleanParameter;
import com.ibm.cohort.engine.parameter.CodeParameter;
import com.ibm.cohort.engine.parameter.DatetimeParameter;
import com.ibm.cohort.engine.parameter.DecimalParameter;
import com.ibm.cohort.engine.parameter.IntegerParameter;
import com.ibm.cohort.engine.parameter.IntervalParameter;
import com.ibm.cohort.engine.parameter.Parameter;
import com.ibm.cohort.engine.parameter.QuantityParameter;
import com.ibm.cohort.engine.parameter.StringParameter;
import com.ibm.cohort.engine.parameter.TimeParameter;

public class ParameterHelper {
	/**
	 * Conversion routine for CQL parameter values encoded for command line
	 * interaction.
	 *
	 * @param arguments list of CQL parameter values encoded as strings
	 * @return decoded parameter values formatted for consumption by the CQL engine
	 */
	public static Map<String, Parameter> parseParameterArguments(List<String> arguments) {
		Map<String, Parameter> result = new HashMap<>();

		Pattern p = Pattern.compile("(?<name>[^:]+):(?<type>[^:]+):(?<value>.*)");
		for (String arg : arguments) {
			Matcher m = p.matcher(arg);
			if (m.matches()) {
				String name = m.group("name");
				String type = m.group("type");
				String value = m.group("value");
				String subType = null;
				String start = null;
				String end = null;
				if (type.equals("interval")) {
					String[] parts = value.split(",");

					if (parts.length != 3) {
						throw new IllegalArgumentException("Invalid interval format. Must contain exactly 3 fields separated by commas");
					}

					subType = parts[0];
					start = parts[1];
					end = parts[2];
				}
				Map.Entry<String, Parameter> stringObjectEntry = convertParameter(name, type, value, subType, start, end);
				result.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
			}
			else {
				throw new IllegalArgumentException(String.format("Invalid parameter string %s", arg));
			}
		}
		return result;
	}
	
	private static Map.Entry<String, Parameter> convertParameter(String name, String type, String value,
															  String subType, String start, String end) {
		Parameter typedValue = null;
		switch (type) {
			case "integer":
				typedValue = new IntegerParameter( Integer.parseInt(value) );
				break;
			case "decimal":
				typedValue = new DecimalParameter( value );
				break;
			case "boolean":
				typedValue = new BooleanParameter( Boolean.parseBoolean(value) );
				break;
			case "string":
				typedValue = new StringParameter( value );
				break;
			case "datetime":
				typedValue = resolveDateTimeParameter(value);
				break;
			case "time":
				typedValue = new TimeParameter(value);
				break;
			case "quantity":
				typedValue = resolveQuantityParameter(value);
				break;
			case "code":
				typedValue = resolveCodeParameter(value);
				break;
			case "concept":
				throw new UnsupportedOperationException("No support for concept type parameters");
			case "interval":
				switch (subType) {
					case "integer":
						typedValue = new IntervalParameter(new IntegerParameter(Integer.parseInt(start)), true, new IntegerParameter(Integer.parseInt(end)), true);
						break;
					case "decimal":
						typedValue = new IntervalParameter(new DecimalParameter(start), true, new DecimalParameter(end), true);
						break;
					case "quantity":
						typedValue = new IntervalParameter(resolveQuantityParameter(start), true, resolveQuantityParameter(end),
								true);
						break;
					case "datetime":
						typedValue = new IntervalParameter(resolveDateTimeParameter(start), true, resolveDateTimeParameter(end),
								true);
						break;
					case "time":
						typedValue = new IntervalParameter(new TimeParameter(start), true, new TimeParameter(end), true);
						break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported interval type %s", subType));
				}
				break;
			default:
				throw new IllegalArgumentException(String.format("Parameter type %s not supported", type));
		}
		return new AbstractMap.SimpleEntry<String,Parameter>(name, typedValue);
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static CodeParameter resolveCodeParameter(String value) {
		CodeParameter typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new CodeParameter().setValue(parts[0]).setSystem(parts[1]).setDisplay(parts[2]);
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static DatetimeParameter resolveDateTimeParameter(String value) {
		DatetimeParameter typedValue;
		typedValue = new DatetimeParameter(value);
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static QuantityParameter resolveQuantityParameter(String value) {
		QuantityParameter typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new QuantityParameter().setAmount(parts[0]).setUnit(parts[1]);
		return typedValue;
	}
}
