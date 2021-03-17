/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKeyTest {

	private static final String CONTEXT = "context";
	private static final String CONTEXT_PATH = "contextPath";
	private static final String CONTEXT_VALUE = "contextValue";
	private static final String DATA_TYPE = "dataType";
	private static final String TEMPLATE_ID = "templateId";
	private static final String CODE_PATH = "codePath";
	private static final List<Code> CODES = Arrays.asList(
			createCode("code1"),
			createCode("code2"),
			createCode("code3")
	);
	private static final String VALUE_SET = "valueSet";

	private static Code createCode(String code) {
		return new Code()
				.withCode(code)
				.withSystem(code + "-system")
				.withDisplay(code + "-display")
				.withVersion(code + "version");
	}

	@Test
	public void create_nullCodes() {
		CacheKey expected = new CacheKey(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				Collections.emptyList(),
				VALUE_SET
		);

		CacheKey actual = CacheKey.create(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				null,
				VALUE_SET
		);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void create_emptyCodes() {
		CacheKey expected = new CacheKey(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				Collections.emptyList(),
				VALUE_SET
		);

		CacheKey actual = CacheKey.create(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				Collections.emptyList(),
				VALUE_SET
		);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void create_actualCodes() {
		CacheKey expected = new CacheKey(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				toCacheCodes(CODES),
				VALUE_SET
		);

		CacheKey actual = CacheKey.create(
				CONTEXT,
				CONTEXT_PATH,
				CONTEXT_VALUE,
				DATA_TYPE,
				TEMPLATE_ID,
				CODE_PATH,
				CODES,
				VALUE_SET
		);

		Assert.assertEquals(expected, actual);
	}

	private List<CacheCode> toCacheCodes(List<Code> cacheCodes) {
		return cacheCodes.stream()
				.map(CacheCode::create)
				.collect(Collectors.toList());
	}
}
