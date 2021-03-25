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
public final class RetrieveCacheCode {

	public static RetrieveCacheCode create(Code code) {
		return new RetrieveCacheCode(code.getCode(), code.getSystem(), code.getDisplay(), code.getVersion());
	}

	private final String code;
	private final String system;
	private final String display;
	private final String version;

	public RetrieveCacheCode(String code, String system, String display, String version) {
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
		RetrieveCacheCode retrieveCacheCode = (RetrieveCacheCode) o;
		return Objects.equals(code, retrieveCacheCode.code)
				&& Objects.equals(system, retrieveCacheCode.system)
				&& Objects.equals(display, retrieveCacheCode.display)
				&& Objects.equals(version, retrieveCacheCode.version);
	}

	@Override
	@Generated
	public int hashCode() {
		return Objects.hash(code, system, display, version);
	}

	@Override
	public String toString() {
		return "RetrieveCacheCode{" +
				"code='" + code + '\'' +
				", system='" + system + '\'' +
				", display='" + display + '\'' +
				", version='" + version + '\'' +
				'}';
	}
}
