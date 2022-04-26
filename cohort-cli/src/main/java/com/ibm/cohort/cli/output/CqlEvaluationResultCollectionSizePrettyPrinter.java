/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import java.util.Collection;

public class CqlEvaluationResultCollectionSizePrettyPrinter extends CqlEvaluationResultPrettyPrinter {
	@Override
	protected StringBuilder handleCollection(Object value) {
		StringBuilder sb = new StringBuilder("Collection: ");
		Collection<?> collection = (Collection<?>) value;
		sb.append(collection.size());
		return sb;
	}
}
