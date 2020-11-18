package com.ibm.cohort.cli.input;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.engine.measure.MeasureContext;

public class MeasureContextProvider {
	public static List<MeasureContext> getMeasureContexts(File input) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		MeasureParameterInput parsedInput = objectMapper.readValue(input, MeasureParameterInput.class);

		// Throws an exception if invalid input is encountered
		parsedInput.validate();

		return parsedInput.getMeasureParameterInputs()
				.stream().map(x -> new MeasureContext(x.getMeasureId(), x.getParameters())).collect(Collectors.toList());
	}
}
