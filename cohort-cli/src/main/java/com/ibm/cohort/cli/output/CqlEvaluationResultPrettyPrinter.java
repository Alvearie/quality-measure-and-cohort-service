/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import com.ibm.cohort.cql.evaluation.CqlEvaluationResult;

public interface CqlEvaluationResultPrettyPrinter {
	String prettyPrintResult(CqlEvaluationResult result);
}
