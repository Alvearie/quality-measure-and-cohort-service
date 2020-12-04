/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Time;

import com.ibm.cohort.cli.input.Parameter;

public class ParameterHelper {
	/**
	 * Conversion routine for CQL parameter values encoded for command line
	 * interaction.
	 *
	 * @param arguments list of CQL parameter values encoded as strings
	 * @return decoded parameter values formatted for consumption by the CQL engine
	 */
	public static Map<String, Object> parseParameterArguments(List<String> arguments) {
		Map<String, Object> result = new HashMap<>();

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
					subType = parts[0];
					start = parts[1];
					end = parts[2];
				}
				Map.Entry<String, Object> stringObjectEntry = convertParameter(name, type, value, subType, start, end);
				result.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
			}
			else {
				throw new IllegalArgumentException(String.format("Invalid parameter string %s", arg));
			}
		}
		return result;
	}
	
	public static Map<String, Object> parseParameters(List<Parameter> parameters) {
		Map<String, Object> result = new HashMap<>();

		if (parameters != null) {
			parameters.forEach(p -> {
				Map.Entry<String, Object> stringObjectEntry = convertParameter(p.getName(), p.getType(), p.getValue(),
																			   p.getSubtype(), p.getStart(), p.getEnd());
				result.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
			});
		}
		return result;
	}
	

	public static Map.Entry<String, Object> convertParameter(String name, String type, String value, 
															 String subType, String start, String end) {
		Object typedValue = null;
		String[] parts = null;
		switch (type) {
			case "integer":
				typedValue = Integer.parseInt(value);
				break;
			case "decimal":
				typedValue = new BigDecimal(value);
				break;
			case "boolean":
				typedValue = Boolean.parseBoolean(value);
				break;
			case "string":
				typedValue = value;
				break;
			case "datetime":
				typedValue = resolveDateTimeParameter(value);
				break;
			case "time":
				typedValue = new Time(value);
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
						typedValue = new Interval(Integer.parseInt(start), true, Integer.parseInt(end), true);
						break;
					case "decimal":
						typedValue = new Interval(new BigDecimal(start), true, new BigDecimal(end), true);
						break;
					case "quantity":
						typedValue = new Interval(resolveQuantityParameter(start), true, resolveQuantityParameter(end),
												  true
						);
						break;
					case "datetime":
						typedValue = new Interval(resolveDateTimeParameter(start), true, resolveDateTimeParameter(end),
												  true
						);
						break;
					case "time":
						typedValue = new Interval(new Time(start), true, new Time(end), true);
						break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported interval type %s", subType));
				}
				break;
			default:
				throw new IllegalArgumentException(String.format("Parameter type %s not supported", type));
		}
		return new AbstractMap.SimpleEntry(name, typedValue);
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static Object resolveCodeParameter(String value) {
		Object typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new Code().withCode(parts[0]).withSystem(parts[1]).withDisplay(parts[2]);
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static Object resolveDateTimeParameter(String value) {
		Object typedValue;
		typedValue = new DateTime(value.replace("@", ""), OffsetDateTime.now().getOffset());
		return typedValue;
	}

	/**
	 * Decode command-line encoded parameter
	 * 
	 * @param value encoded parameter value
	 * @return decoded parameter value
	 */
	public static Object resolveQuantityParameter(String value) {
		Object typedValue;
		String[] parts;
		parts = value.trim().split(":");
		typedValue = new Quantity().withValue(new BigDecimal(parts[0])).withUnit(parts[1]);
		return typedValue;
	}
}
