/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.measure.seed;

import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

public interface IMeasureEvaluationSeed {
	Measure getMeasure();

	Context getContext();

	Interval getMeasurementPeriod();

	DataProvider getDataProvider();
}
