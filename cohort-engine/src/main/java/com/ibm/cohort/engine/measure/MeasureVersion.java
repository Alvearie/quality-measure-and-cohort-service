/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MeasureVersion implements Comparable<MeasureVersion> {
	private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile("^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)");

	public static Optional<MeasureVersion> create(String version) {
		if (version != null) {
			Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(version);
			if (matcher.matches()) {
				return Optional.of(
						new MeasureVersion(Integer.valueOf(matcher.group("major")),
										   Integer.valueOf(matcher.group("minor")), Integer.valueOf(matcher.group("patch"))
						));
			}
		}
		return Optional.empty();
	}
	
	private final int major;
	private final int minor;
	private final int patch;

	public MeasureVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MeasureVersion that = (MeasureVersion) o;

		if (major != that.major) return false;
		if (minor != that.minor) return false;
		return patch == that.patch;

	}

	@Override
	public int hashCode() {
		int result = major;
		result = 31 * result + minor;
		result = 31 * result + patch;
		return result;
	}

	@Override
	public int compareTo(MeasureVersion o) {
		if (this.major != o.major) {
			return this.major - o.major;
		} else if (this.minor != o.minor) {
			return this.minor - o.minor;
		} else {
			return this.patch - o.patch;
		}
	}
}
