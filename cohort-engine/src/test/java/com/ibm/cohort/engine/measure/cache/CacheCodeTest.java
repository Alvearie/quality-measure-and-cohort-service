/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;

public class CacheCodeTest {

	@Test
	public void create() {
		String code = "code";
		String system = "system";
		String display = "display";
		String version = "version";

		Code source = new Code()
				.withCode(code)
				.withSystem(system)
				.withDisplay(display)
				.withVersion(version);

		CacheCode expected = new CacheCode(code, system, display, version);
		CacheCode actual = CacheCode.create(source);

		Assert.assertEquals(expected, actual);
	}

}
