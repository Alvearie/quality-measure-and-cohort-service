/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cqfruler;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.Context;

/**
 * 
 * This class extends the base context with the purpose of removing the limit on the number of items stored in the expression cache.  
 * As this is expected to cleared between measure executions per patient, storing all of the define results shouldn't break the bank on memory.
 *
 */
public class CDMContext extends Context {

    private Map<VersionedIdentifier, Map<String, Object>> expressions = new LinkedHashMap<VersionedIdentifier, Map<String, Object>>(10, 0.9f, true) {
		private static final long serialVersionUID = 2837966863351097165L;
	};
    
    private Map<String, Object> constructNoEvictLibraryExpressionHashMap() {
        return new LinkedHashMap<>(15, 0.9f, true);
    }
    
	@Override
	public boolean isExpressionInCache(VersionedIdentifier libraryId, String name) {
        if (!this.expressions.containsKey(libraryId)) {
            this.expressions.put(libraryId, constructNoEvictLibraryExpressionHashMap());
        }

        return this.expressions.get(libraryId).containsKey(name);
    }
	
	@Override
	public void addExpressionToCache(VersionedIdentifier libraryId, String name, Object result) {
        if (!this.expressions.containsKey(libraryId)) {
            this.expressions.put(libraryId, constructNoEvictLibraryExpressionHashMap());
        }

        this.expressions.get(libraryId).put(name, result);
    }

	@Override
	public Object getExpressionResultFromCache(VersionedIdentifier libraryId, String name) {
		this.expressions.computeIfAbsent(libraryId, x -> constructNoEvictLibraryExpressionHashMap());
		
        return this.expressions.get(libraryId).get(name);
    }
	
	public void clearExpressionCache() {
		expressions.clear();
	}

	public CDMContext(Library library) {
		super(library);
	}
	
	public Set<VersionedIdentifier> getLibrariesInCache() {
		return expressions.keySet();
	}
	
	public Set<Entry<VersionedIdentifier, Map<String, Object>>> getEntriesInCache() {
		return expressions.entrySet();
	}
}
