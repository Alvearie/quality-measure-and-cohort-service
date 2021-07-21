/*
 * (C) Copyright IBM Copr. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
