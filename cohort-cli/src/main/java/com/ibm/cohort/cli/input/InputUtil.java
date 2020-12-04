/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli.input;

public class InputUtil {
	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}
}
