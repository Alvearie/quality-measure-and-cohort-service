/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cli.ParameterHelper;
import com.ibm.cohort.measure.Identifier;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.measure.MeasureContext;


public class MeasureContextProvider {
	private MeasureContextProvider() {}
	
	public static List<MeasureContext> getMeasureContexts(File input) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ExecutionConfiguration parsedInput = objectMapper.readValue(input, ExecutionConfiguration.class);

		// Throws an exception if invalid input is encountered
		parsedInput.validate();

		return parsedInput.getMeasureConfigurations()
				.stream().map(x -> {
					MeasureIdentifier measureIdentifier = x.getIdentifier();
					Identifier identifier = null;
					if (measureIdentifier != null) {
						identifier = new Identifier(measureIdentifier.getSystem(), measureIdentifier.getValue());
					}
					return new MeasureContext(x.getMeasureId(), x.getParameters(), identifier, x.getVersion());
				}).collect(Collectors.toList());
	}

	public static List<MeasureContext> getMeasureContexts(String resourceId, List<String> parameters) {
		Map<String, Parameter> parsedParameters = null;
		if (parameters != null) {
			parsedParameters = ParameterHelper.parseParameterArguments(parameters);
		}

		return Collections.singletonList(new MeasureContext(resourceId, parsedParameters));
	}
}
