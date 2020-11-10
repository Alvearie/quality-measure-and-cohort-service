/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

public interface MeasureResolutionProvider<MeasureType> {
	public MeasureType resolveMeasureById(String resourceID);
	public MeasureType resolveMeasureByCanonicalUrl(String url);
	public MeasureType resolveMeasureByName(String name, String version);
}
