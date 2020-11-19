/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class FhirClientBuilderFactoryTest {
	@Test
	public void testNoImpl__getDefault() {
		FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
		assertNotNull(factory);
		assertEquals( FhirClientBuilderFactory.DEFAULT_IMPL_CLASS_NAME, factory.getClass().getName() );
	}
	
	@Test
	public void testWithImpl__returnsClass() {
		System.setProperty( FhirClientBuilderFactory.IMPL_CLASS_NAME, DummyFhirClientBuilderFactory.class.getName() );
		try {
			FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
			assertNotNull(factory);
			assertEquals( DummyFhirClientBuilderFactory.class.getName(), factory.getClass().getName() );
		} finally {
			System.getProperties().remove( FhirClientBuilderFactory.IMPL_CLASS_NAME );
		}
	}
}
