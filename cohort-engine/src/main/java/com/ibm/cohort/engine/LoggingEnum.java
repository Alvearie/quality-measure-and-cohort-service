/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.engine;

public enum LoggingEnum {
	COVERAGE("coverage"),
	TRACE("trace"),
	NA(null);

	private String debugLevel;

	LoggingEnum(String debugLevel){
		this.debugLevel = debugLevel;
	}

	public static LoggingEnum getLoggingFromString(String value){

		try{
			return LoggingEnum.valueOf(value);
		}
		//todo only need to catch IllegalArgument and NPE
		catch(Exception e) {
			return LoggingEnum.NA;
		}
	}

	public String getDebugLevel() {
		return debugLevel;
	}
}
