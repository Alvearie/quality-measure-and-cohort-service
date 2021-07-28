/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.cohort.datarow.model.CodeKey;
import com.ibm.cohort.datarow.model.DataRow;

/**
 * This is an implementation of the CQL RetrieveProvider interface for input
 * data that is a Map of <code>dataType</code> strings to lists of DataRow
 * objects. Retrieval is optionally filtered by the <code>codePath</code> when
 * provided. Codes are indexed on first use for faster retrieval on subsequent
 * data operations. Date range filtering is not supported.
 */
public class DataRowRetrieveProvider implements RetrieveProvider {

	private final Map<String, Iterable<Object>> data;

	private Map<String, Map<String, Map<Object, List<Object>>>> indexes;

	private TerminologyProvider terminologyProvider;

	public DataRowRetrieveProvider(Map<String, Iterable<Object>> data, TerminologyProvider terminologyProvider) {
		this.data = data;
		this.indexes = new HashMap<>();
		this.terminologyProvider = terminologyProvider;
	}

	@Override
	public Iterable<Object> retrieve(String context, String contextPath, Object contextValue, String dataType,
			String templateId, String codePath, Iterable<Code> codes, String valueSet, String datePath,
			String dateLowPath, String dateHighPath, Interval dateRange) {
		Iterable<Object> result;

		// Fast fail for an unsupported operation scenario
		if (datePath != null || dateLowPath != null || dateHighPath != null) {
			throw new UnsupportedOperationException("Date-based filtering is not supported at this time.");
		}

		Iterable<Object> allRows = data.get(dataType);
		if (codePath != null) {
			// Calculate an index of code to matching rows based on the dataType and
			// codePath
			Map<String, Map<Object, List<Object>>> codePathToCodeMap = indexes.computeIfAbsent(dataType,
					key -> new HashMap<>());
			Map<Object, List<Object>> indexedRows = codePathToCodeMap.computeIfAbsent(codePath, key -> {
				Map<Object, List<Object>> codeMap = new HashMap<>();
				if (allRows != null) {
					for (Object obj : allRows) {
						DataRow row = (DataRow) obj;
						Object code = row.getValue(codePath);
						if (code instanceof String) {
							code = new CodeKey().withCode((String) code);
						} else if (code instanceof Code) {
							code = new CodeKey((Code) code);
						}

						List<Object> list = codeMap.computeIfAbsent(code, codeKey -> new ArrayList<>());
						list.add(row);
					}
				}
				return codeMap;
			});

			if (valueSet != null) {
				// expand the valueset into codes
				ValueSetInfo valueSetInfo = new ValueSetInfo().withId(valueSet);
				codes = terminologyProvider.expand(valueSetInfo);
			}
			if (codes != null) {
				List<Object> allMatches = new ArrayList<>();
				for (Code codeToCheck : codes) {
					CodeKey indexKey = new CodeKey(codeToCheck);
					List<Object> matches = indexedRows.get(indexKey);
					if (matches != null) {
						allMatches.addAll(matches);
					}
				}
				result = allMatches;
			} else {
				throw new IllegalArgumentException(String.format(
						"No codes found for filtered retrieve of dataType %s, codePath %s", dataType, codePath));
			}
		} else {
			result = (allRows != null) ? allRows : Collections.emptyList();
		}

		return result;
	}
}
