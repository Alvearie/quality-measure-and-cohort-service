/*
 * (C) Copyright IBM Copr. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.r4.cache;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;

public class CachingModelResolverDecoratorTest {

	private TestCachingModelResolverDecorator modelResolver;
	private ModelResolver innerModelResolver;
	
	@Before
	public void initializeModelResolver() {
		this.innerModelResolver = spy(new R4FhirModelResolver());
		this.modelResolver = new TestCachingModelResolverDecorator(this.innerModelResolver);
		this.modelResolver.clearCaches();
	}
	
	@Test
	public void test_caching_type_resolution_by_type__inner_method_called_once() {
		Class<?> expectedClass = Patient.class;
		Class<?> actual1 = this.modelResolver.resolveType("Patient");
		Class<?> actual2 = this.modelResolver.resolveType("Patient");
		
		assertEquals(expectedClass, actual1);
		assertEquals(actual1, actual2);
		
		verify(this.innerModelResolver, times(1)).resolveType("Patient");
	}

	@Test
	public void test_caching_type_resolution_by_class__inner_method_called_once() {
		Class<?> expectedClass = Patient.class;
		Class<?> actual1 = this.modelResolver.resolveType(new Patient());
		Class<?> actual2 = this.modelResolver.resolveType(new Patient());

		assertEquals(expectedClass, actual1);
		assertEquals(actual1, actual2);

		verify(this.innerModelResolver, times(1)).resolveType(any(Patient.class));
	}

	@Test
	public void test_caching_context_path__inner_method_called_once() {
		String expectedClass = "id";
		Object actual1 = this.modelResolver.getContextPath("Patient", "Patient");
		Object actual2 = this.modelResolver.getContextPath("Patient", "Patient");

		assertTrue(actual1 instanceof String);
		assertTrue(actual2 instanceof String);

		assertEquals(expectedClass, actual1);
		assertEquals(actual1, actual2);

		verify(this.innerModelResolver, times(1)).getContextPath("Patient", "Patient");
	}
}