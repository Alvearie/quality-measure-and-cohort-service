/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cache;

import com.ibm.cohort.cql.cache.RetrieveCacheCode;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;

public class RetrieveCacheCodeTest {

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

		RetrieveCacheCode expected = new RetrieveCacheCode(code, system, display, version);
		RetrieveCacheCode actual = RetrieveCacheCode.create(source);

		Assert.assertEquals(expected, actual);
	}

}
