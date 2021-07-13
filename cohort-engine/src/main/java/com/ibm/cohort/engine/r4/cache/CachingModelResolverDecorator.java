package com.ibm.cohort.engine.r4.cache;

import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import org.opencds.cqf.cql.engine.model.ModelResolver;


public class CachingModelResolverDecorator implements ModelResolver {
	private static final String CACHE_PREFIX = "model-resolver";
	
	private static final String PER_PACKAGE_CONTEXT_RESOLUTION_CACHE_PREFIX = CACHE_PREFIX + "-context-resolution";
	private static final String PER_PACKAGE_TYPE_RESOLUTION_BY_NAME_CACHE_PREFIX = CACHE_PREFIX + "-type-resolution-by-name";
	private static final String PER_PACKAGE_TYPE_RESOLUTION_BY_CLASS_CACHE_PREFIX = CACHE_PREFIX + "-type-resolution-by-class";
	
	private static Map<String, Map<String, Cache<String, Object>>> perPackageContextResolutions = new HashMap<>();
	private static Map<String, Cache<String,Class<?>>> perPackageTypeResolutionsByTypeName = new HashMap<>();
	private static Map<String, Cache<Class<?>,Class<?>>> perPackageTypeResolutionsByClass = new HashMap<>();
	

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

		Map<String,Cache<String, Object>> packageContextResolutions = perPackageContextResolutions.get(this.getPackageName());

		if (!packageContextResolutions.containsKey(contextType)) {
			packageContextResolutions.put(
					contextType,
					Caching.getCachingProvider().getCacheManager().createCache(
							PER_PACKAGE_CONTEXT_RESOLUTION_CACHE_PREFIX + "-" + this.getPackageName(),
							new MutableConfiguration<String, Object>().setStoreByValue(false)
					)
			);
		}

		Cache<String, Object> contextTypeResolutions = packageContextResolutions.get(contextType);
		if (!contextTypeResolutions.containsKey(targetType)) {
			contextTypeResolutions.put(targetType, this.innerResolver.getContextPath(contextType, targetType));
		}

		return contextTypeResolutions.get(targetType);
	}

	@Override
	public Class<?> resolveType(String typeName) {
		if (!perPackageTypeResolutionsByTypeName.containsKey(this.getPackageName())) {
			perPackageTypeResolutionsByTypeName.put(
					this.getPackageName(),
					Caching.getCachingProvider().getCacheManager().createCache(
							PER_PACKAGE_TYPE_RESOLUTION_BY_NAME_CACHE_PREFIX + "-" + this.getPackageName(),
							new MutableConfiguration<String, Class<?>>().setStoreByValue(false)
					)
			);
		}

		Cache<String, Class<?>> packageTypeResolutions = perPackageTypeResolutionsByTypeName.get(this.getPackageName());
		if (!packageTypeResolutions.containsKey(typeName)) {
			packageTypeResolutions.put(typeName, this.innerResolver.resolveType(typeName));
		}

		return packageTypeResolutions.get(typeName);
	}

	@Override
	public Class<?> resolveType(Object value) {
		if (!perPackageTypeResolutionsByClass.containsKey(this.getPackageName())) {
			perPackageTypeResolutionsByClass.put(
					this.getPackageName(),
					Caching.getCachingProvider().getCacheManager().createCache(
							PER_PACKAGE_TYPE_RESOLUTION_BY_CLASS_CACHE_PREFIX + "-" + this.getPackageName(),
							new MutableConfiguration<Class<?>, Class<?>>().setStoreByValue(false)
					)
			);
		}

		Cache<Class<?>, Class<?>> packageTypeResolutions = perPackageTypeResolutionsByClass.get(this.getPackageName());

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
