/*
 * (C) Copyright IBM Corp. 2020, 2020
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
import com.ibm.cohort.engine.measure.MeasureContext;

public class MeasureContextProvider {
	public static List<MeasureContext> getMeasureContexts(File input) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		MeasureParameters parsedInput = objectMapper.readValue(input, MeasureParameters.class);

		// Throws an exception if invalid input is encountered
		parsedInput.validate();

		return parsedInput.getMeasureConfigurations()
				.stream().map(x -> new MeasureContext(x.getMeasureId(), x.getParameters())).collect(Collectors.toList());
	}

	public static List<MeasureContext> getMeasureContexts(String resourceId, List<String> parameters) {
		Map<String, Object> parsedParameters = null;
		if (parameters != null) {
			parsedParameters = ParameterHelper.parseParameterArguments(parameters);
		}

		return Collections.singletonList(new MeasureContext(resourceId, parsedParameters));
	}
}
