/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure.cache;

import com.ibm.cohort.annotations.Generated;
import org.opencds.cqf.cql.engine.runtime.Code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A HashMap friendly model representing the cacheable fields of `RetrieveProvider.retrieve()`.
 * @see org.opencds.cqf.cql.engine.retrieve.RetrieveProvider
 */
public class CacheKey {

	public static CacheKey create(
			String context,
			String contextPath,
			String contextValue,
			String dataType,
			String templateId,
			String codePath,
			Iterable<Code> codes,
			String valueSet
	) {
		List<CacheCode> cacheCodes = Collections.emptyList();
		if (codes != null) {
			cacheCodes = new ArrayList<>();
			for (Code code : codes) {
				cacheCodes.add(CacheCode.create(code));
			}
		}
		return new CacheKey(context, contextPath, contextValue, dataType, templateId, codePath, cacheCodes, valueSet);
	}

	private final String context;
	private final String contextPath;
	private final String contextValue;
	private final String dataType;
	private final String templateId;
	private final String codePath;
	private final List<CacheCode> codes;
	private final String valueSet;

	public CacheKey(
			String context,
			String contextPath,
			String contextValue,
			String dataType,
			String templateId,
			String codePath,
			List<CacheCode> codes,
			String valueSet
	) {
		this.context = context;
		this.contextPath = contextPath;
		this.contextValue = contextValue;
		this.dataType = dataType;
		this.templateId = templateId;
		this.codePath = codePath;
		this.codes = codes;
		this.valueSet = valueSet;
	}

	@Override
	@Generated
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheKey cacheKey = (CacheKey) o;
		return Objects.equals(context, cacheKey.context)
				&& Objects.equals(contextPath, cacheKey.contextPath)
				&& Objects.equals(contextValue, cacheKey.contextValue)
				&& Objects.equals(dataType, cacheKey.dataType)
				&& Objects.equals(templateId, cacheKey.templateId)
				&& Objects.equals(codePath, cacheKey.codePath)
				&& Objects.equals(codes, cacheKey.codes)
				&& Objects.equals(valueSet, cacheKey.valueSet);
	}

	@Override
	@Generated
	public int hashCode() {
		return Objects.hash(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet);
	}

}
