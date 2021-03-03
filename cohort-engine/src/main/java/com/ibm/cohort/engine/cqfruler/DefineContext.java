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

public class DefineContext extends Context {

	@SuppressWarnings("serial")
    private Map<VersionedIdentifier, Map<String, Object>> expressions = new LinkedHashMap<VersionedIdentifier, Map<String, Object>>(10, 0.9f, true) {
//		protected boolean removeEldestEntry(Map.Entry<VersionedIdentifier, LinkedHashMap<String, Object>> eldestEntry) {
//            return size() > 10;
//        }
    };
    
    private Map<String, Object> constructLibraryExpressionHashMap() {
        return new LinkedHashMap<>(15, 0.9f, true);
//        {
//            protected boolean removeEldestEntry(Map.Entry<String, Object> eldestEntry) {
//                return size() > 15;
//            }
//        };
    }
    
	public DefineContext(Library library) {
		super(library);
	}
	
	public Set<VersionedIdentifier> getLibrariesInCache() {
		return expressions.keySet();
	}
	
	public Set<Entry<VersionedIdentifier, Map<String, Object>>> getEntriesInCache() {
		return expressions.entrySet();
	}
	
	@Override
	public boolean isExpressionInCache(VersionedIdentifier libraryId, String name) {
        if (!this.expressions.containsKey(libraryId)) {
            this.expressions.put(libraryId, constructLibraryExpressionHashMap());
        }

        return this.expressions.get(libraryId).containsKey(name);
    }
	
	@Override
	public void addExpressionToCache(VersionedIdentifier libraryId, String name, Object result) {
        if (!this.expressions.containsKey(libraryId)) {
            this.expressions.put(libraryId, constructLibraryExpressionHashMap());
        }

        this.expressions.get(libraryId).put(name, result);
    }

	@Override
	public Object getExpressionResultFromCache(VersionedIdentifier libraryId, String name) {
        if (!this.expressions.containsKey(libraryId)) {
            this.expressions.put(libraryId, constructLibraryExpressionHashMap());
        }

        return this.expressions.get(libraryId).get(name);
    }
	
	public void clearExpressionCache() {
		expressions.clear();
	}

}
