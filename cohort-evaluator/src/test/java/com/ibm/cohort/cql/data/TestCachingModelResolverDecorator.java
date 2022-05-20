/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import com.ibm.cohort.cql.data.CachingModelResolverDecorator;
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
