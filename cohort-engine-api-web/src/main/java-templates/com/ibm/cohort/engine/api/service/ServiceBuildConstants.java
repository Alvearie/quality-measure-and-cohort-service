/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

public class ServiceBuildConstants {
	// will be replaced by Maven
	public static final String DATE = "${build.date}";
	public static final String TIMESTAMP = "${build.timestamp}";
	public static final String VERSION = "${service.version}";
}
