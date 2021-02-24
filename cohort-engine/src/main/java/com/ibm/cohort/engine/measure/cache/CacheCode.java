/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import com.ibm.cohort.annotations.Generated;
import org.opencds.cqf.cql.engine.runtime.Code;

import java.util.Objects;

/**
 * A HashMap friendly version of the CQL engine's `Code` class.
 * @see org.opencds.cqf.cql.engine.runtime.Code
 */
public final class CacheCode {

	public static CacheCode create(Code code) {
		return new CacheCode(code.getCode(), code.getSystem(), code.getDisplay(), code.getVersion());
	}

	private final String code;
	private final String system;
	private final String display;
	private final String version;

	public CacheCode(String code, String system, String display, String version) {
		this.code = code;
		this.system = system;
		this.display = display;
		this.version = version;
	}

	@Override
	@Generated
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheCode cacheCode = (CacheCode) o;
		return Objects.equals(code, cacheCode.code)
				&& Objects.equals(system, cacheCode.system)
				&& Objects.equals(display, cacheCode.display)
				&& Objects.equals(version, cacheCode.version);
	}

	@Override
	@Generated
	public int hashCode() {
		return Objects.hash(code, system, display, version);
	}

}
