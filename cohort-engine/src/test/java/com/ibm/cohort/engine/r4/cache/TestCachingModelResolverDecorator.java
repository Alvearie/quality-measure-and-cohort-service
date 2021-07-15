package com.ibm.cohort.engine.r4.cache;

import org.opencds.cqf.cql.engine.model.ModelResolver;

public class TestCachingModelResolverDecorator extends CachingModelResolverDecorator {
	public TestCachingModelResolverDecorator(ModelResolver modelResolver) {
		super(modelResolver);
	}
	
	public void clearCaches() {
		perPackageContextResolutions.clear();
		perPackageTypeResolutionsByClass.clear();
		perPackageTypeResolutionsByTypeName.clear();
	}
}
