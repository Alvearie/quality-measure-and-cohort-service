package com.ibm.cohort.cli.input;

import java.util.Collections;
import java.util.List;

import com.beust.jcommander.converters.IParameterSplitter;

public class NoSplittingSplitter implements IParameterSplitter {
	@Override
	public List<String> split(String value) {
		return Collections.singletonList(value);
	}
}

