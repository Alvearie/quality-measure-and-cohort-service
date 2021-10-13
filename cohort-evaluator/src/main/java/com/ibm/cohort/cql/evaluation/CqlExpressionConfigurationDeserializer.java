/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.evaluation;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class CqlExpressionConfigurationDeserializer extends StdDeserializer<CqlExpressionConfiguration> {
	public CqlExpressionConfigurationDeserializer() {
		this(null);
	}

	public CqlExpressionConfigurationDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public CqlExpressionConfiguration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		CqlExpressionConfiguration retVal = new CqlExpressionConfiguration();

		JsonNodeType nodeType = node.getNodeType();
		if (nodeType == JsonNodeType.OBJECT) {
			node.fieldNames().forEachRemaining(x -> {
				if (!x.equals("name") && !x.equals("outputColumn")) {
					throw new IllegalArgumentException("Unrecognized field " + x);
				}
			});

			JsonNode name = node.get("name");
			JsonNode outputColumn = node.get("outputColumn");

			if (name == null) {
				throw new IllegalArgumentException("CqlExpressionConfiguration must specify 'name' field. " + node);
			}

			if (name.getNodeType() != JsonNodeType.STRING) {
				throw new IllegalArgumentException("Error parsing 'name'. Expected string but got " + name.getNodeType());
			}
			retVal.setName(name.asText());

			if (outputColumn != null) {
				if (outputColumn.getNodeType() != JsonNodeType.NULL) {
					if (outputColumn.getNodeType() != JsonNodeType.STRING) {
						throw new IllegalArgumentException("Error parsing 'outputColumn'. Expected string but got " + name.getNodeType());
					}
					retVal.setOutputColumn(outputColumn.asText());
				}
			}
		}
		else if(nodeType == JsonNodeType.STRING) {
			retVal.setName(node.asText());
		}
		else {
			throw new IllegalArgumentException("JSON parsing failed. Expected string or object, but found " + nodeType + " for " + node);
		}


		return retVal;
	}
}
