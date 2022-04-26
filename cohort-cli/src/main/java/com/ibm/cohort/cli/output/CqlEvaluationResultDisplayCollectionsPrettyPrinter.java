/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli.output;

import java.util.ArrayList;
import java.util.Collection;

public class CqlEvaluationResultDisplayCollectionsPrettyPrinter extends CqlEvaluationResultPrettyPrinter{
	@Override
	protected StringBuilder handleCollection(Object value) {
		StringBuilder sb = new StringBuilder();
		ArrayList<?> objects = new ArrayList<>((Collection<?>) value);
		sb.append('[');
		if (!objects.isEmpty()) {
			// Append , after each item except the last
			for (int i = 0; i < objects.size() - 1; i++) {
				prettyPrintValue(sb, objects.get(i));
				sb.append(", ");
			}
			prettyPrintValue(sb, objects.get(objects.size() - 1));
		}
		sb.append(']');
		return sb;
	}

}
