/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.valueset;

import com.ibm.cohort.annotations.Generated;

/*
 * This is a simple class to allow a codeSystem and it's version to be used as a key in a hashMap
 */
@Generated
class CodeSystemKey {

	public String codeSystem;
	public String codeSystemVersion;

	public CodeSystemKey(String codeSystem, String codeSystemVersion) {
		this.codeSystem = codeSystem;
		this.codeSystemVersion = codeSystemVersion;
	}

	public String getCodeSystem() {
		return codeSystem;
	}

	public String getCodeSystemVersion() {
		return codeSystemVersion;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CodeSystemKey key = (CodeSystemKey) o;
		if (codeSystem != null ? !codeSystem.equals(key.codeSystem) : key.codeSystem != null) {
			return false;
		}

		if (codeSystemVersion != null ? !codeSystemVersion.equals(key.codeSystemVersion)
				: key.codeSystemVersion != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = codeSystem != null ? codeSystem.hashCode() : 0;
		result = 31 * result + (codeSystemVersion != null ? codeSystemVersion.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "[" + codeSystem + ", " + codeSystemVersion + "]";
	}
}