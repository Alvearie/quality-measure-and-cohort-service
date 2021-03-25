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
public class RetrieveCacheKey {

	public static RetrieveCacheKey create(
			String context,
			String contextPath,
			String contextValue,
			String dataType,
			String templateId,
			String codePath,
			Iterable<Code> codes,
			String valueSet
	) {
		// POST PR TODO: Is a null list or empty list better for persistence?
		List<RetrieveCacheCode> retrieveCacheCodes = Collections.emptyList();
		if (codes != null) {
			retrieveCacheCodes = new ArrayList<>();
			for (Code code : codes) {
				retrieveCacheCodes.add(RetrieveCacheCode.create(code));
			}
		}
		return new RetrieveCacheKey(context, contextPath, contextValue, dataType, templateId, codePath, retrieveCacheCodes, valueSet);
	}

	private final String context;
	private final String contextPath;
	private final String contextValue;
	private final String dataType;
	private final String templateId;
	private final String codePath;
	private final List<RetrieveCacheCode> codes;
	private final String valueSet;

	public RetrieveCacheKey(
			String context,
			String contextPath,
			String contextValue,
			String dataType,
			String templateId,
			String codePath,
			List<RetrieveCacheCode> codes,
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
		RetrieveCacheKey retrieveCacheKey = (RetrieveCacheKey) o;
		return Objects.equals(context, retrieveCacheKey.context)
				&& Objects.equals(contextPath, retrieveCacheKey.contextPath)
				&& Objects.equals(contextValue, retrieveCacheKey.contextValue)
				&& Objects.equals(dataType, retrieveCacheKey.dataType)
				&& Objects.equals(templateId, retrieveCacheKey.templateId)
				&& Objects.equals(codePath, retrieveCacheKey.codePath)
				&& Objects.equals(codes, retrieveCacheKey.codes)
				&& Objects.equals(valueSet, retrieveCacheKey.valueSet);
	}

	@Override
	@Generated
	public int hashCode() {
		return Objects.hash(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet);
	}

	@Override
	public String toString() {
		return "RetrieveCacheKey{" +
				"context='" + context + '\'' +
				", contextPath='" + contextPath + '\'' +
				", contextValue='" + contextValue + '\'' +
				", dataType='" + dataType + '\'' +
				", templateId='" + templateId + '\'' +
				", codePath='" + codePath + '\'' +
				", codes=" + codes +
				", valueSet='" + valueSet + '\'' +
				'}';
	}
}
