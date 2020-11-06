/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

public class ParameterHelper {
	/**
	 * Conversion routine for CQL parameter values encoded for command line
	 * interaction.
	 * 
	 * @param arguments list of CQL parameter values encoded as strings
	 * @return decoded parameter values formatted for consumption by the CQL engine
	 */
	public static Map<String, Object> parseParameters(List<String> arguments) {
		Map<String, Object> result = new HashMap<>();

		Pattern p = Pattern.compile("(?<name>[^:]+):(?<type>[^:]+):(?<value>.*)");
		for (String arg : arguments) {
			Matcher m = p.matcher(arg);
			if (m.matches()) {
				String name = m.group("name");
				String type = m.group("type");
				String value = m.group("value");

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
					parts = value.split(",");
					String subType = parts[0];
					String start = parts[1];
					String end = parts[2];

					switch (subType) {
					case "integer":
						typedValue = new Interval(Integer.parseInt(start), true, Integer.parseInt(end), true);
						break;
					case "decimal":
						typedValue = new Interval(new BigDecimal(start), true, new BigDecimal(end), true);
						break;
					case "quantity":
						typedValue = new Interval(resolveQuantityParameter(start), true, resolveQuantityParameter(end),
								true);
						break;
					case "datetime":
						typedValue = new Interval(resolveDateTimeParameter(start), true, resolveDateTimeParameter(end),
								true);
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

				result.put(name, typedValue);
			} else {
				throw new IllegalArgumentException(String.format("Invalid parameter string %s", arg));
			}
		}

		return result;
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
