/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.flink.execution;

import java.io.Serializable;

public class CacheConfiguration implements Serializable {

	private static final long serialVersionUID = -1312955467512758710L;

	private int maxSize;
	private int expireOnWrite;
	private boolean enableStatistics;

	public CacheConfiguration() { }

	public CacheConfiguration(int maxSize, int expireOnWrite, boolean enableStatistics) {
		this.maxSize = maxSize;
		this.expireOnWrite = expireOnWrite;
		this.enableStatistics = enableStatistics;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getExpireOnWrite() {
		return expireOnWrite;
	}

	public void setExpireOnWrite(int expireOnWrite) {
		this.expireOnWrite = expireOnWrite;
	}

	public boolean isEnableStatistics() {
		return enableStatistics;
	}

	public void setEnableStatistics(boolean enableStatistics) {
		this.enableStatistics = enableStatistics;
	}

	@Override
	public String toString() {
		return "CacheConfiguration{" +
				"maxSize=" + maxSize +
				", expireOnWrite=" + expireOnWrite +
				", enableStatistics=" + enableStatistics +
				'}';
	}

}
