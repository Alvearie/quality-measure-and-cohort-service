/*
 * (C) Copyright IBM Copr. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.r4.cache;

import java.util.HashMap;
import java.util.Map;

import org.opencds.cqf.cql.engine.model.ModelResolver;

// copy/pasted from CQL Engine 1.5.1. Revisit on upgrade of that dependency
public class CachingModelResolverDecorator implements ModelResolver {
	static Map<String, Map<String,Map<String, Object>>> perPackageContextResolutions = new HashMap<>();
	static Map<String, Map<String,Class<?>>> perPackageTypeResolutionsByTypeName = new HashMap<>();
	static Map<String, Map<Class<?>,Class<?>>> perPackageTypeResolutionsByClass = new HashMap<>();

	private ModelResolver innerResolver;

	public CachingModelResolverDecorator(ModelResolver modelResolver) {
		this.innerResolver = modelResolver;
	}

	@Override
	public String getPackageName() {
		return this.innerResolver.getPackageName();
	}

	@Override
	public void setPackageName(String packageName) {
		this.innerResolver.setPackageName(packageName);
	}

	@Override
	public Object resolvePath(Object target, String path) {
		return this.innerResolver.resolvePath(target, path);
	}

	@Override
	public Object getContextPath(String contextType, String targetType) {
		if (!perPackageContextResolutions.containsKey(this.getPackageName())) {
			perPackageContextResolutions.put(this.getPackageName(),  new HashMap<>());
		}

		Map<String,Map<String, Object>> packageContextResolutions = perPackageContextResolutions.get(this.getPackageName());

		if (!packageContextResolutions.containsKey(contextType)) {
			packageContextResolutions.put(contextType, new HashMap<>());
		}

		Map<String, Object> contextTypeResolutions = packageContextResolutions.get(contextType);
		if (!contextTypeResolutions.containsKey(targetType)) {
			contextTypeResolutions.put(targetType, this.innerResolver.getContextPath(contextType, targetType));
		}

		return contextTypeResolutions.get(targetType);
	}

	@Override
	public Class<?> resolveType(String typeName) {
		if (!perPackageTypeResolutionsByTypeName.containsKey(this.getPackageName())) {
			perPackageTypeResolutionsByTypeName.put(this.getPackageName(), new HashMap<>());
		}

		Map<String, Class<?>> packageTypeResolutions = perPackageTypeResolutionsByTypeName.get(this.getPackageName());
		if (!packageTypeResolutions.containsKey(typeName)) {
			packageTypeResolutions.put(typeName, this.innerResolver.resolveType(typeName));
		}

		return packageTypeResolutions.get(typeName);
	}

	@Override
	public Class<?> resolveType(Object value) {
		if (!perPackageTypeResolutionsByClass.containsKey(this.getPackageName())) {
			perPackageTypeResolutionsByClass.put(this.getPackageName(), new HashMap<>());
		}

		Map<Class<?>, Class<?>> packageTypeResolutions = perPackageTypeResolutionsByClass.get(this.getPackageName());

		Class<?> valueClass = value.getClass();
		if (!packageTypeResolutions.containsKey(valueClass)) {
			packageTypeResolutions.put(valueClass, this.innerResolver.resolveType(value));
		}

		return packageTypeResolutions.get(valueClass);
	}

	@Override
	public Object createInstance(String typeName) {
		return this.innerResolver.createInstance(typeName);
	}

	@Override
	public void setValue(Object target, String path, Object value) {
		this.innerResolver.setValue(target, path, value);
	}

	@Override
	public Boolean objectEqual(Object left, Object right) {
		return this.innerResolver.objectEqual(left, right);
	}

	@Override
	public Boolean objectEquivalent(Object left, Object right) {
		return this.innerResolver.objectEquivalent(left, right);
	}

	@Override
	public Boolean is(Object value, Class<?> type) {
		return this.innerResolver.is(value, type);
	}

	@Override
	public Object as(Object value, Class<?> type, boolean isStrict) {
		return this.innerResolver.as(value, type, isStrict);
	}

	public ModelResolver getInnerResolver() {
		return this.innerResolver;
	}
}
