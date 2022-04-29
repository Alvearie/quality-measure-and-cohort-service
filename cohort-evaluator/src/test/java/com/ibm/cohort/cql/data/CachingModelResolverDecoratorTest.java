/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.model.ModelResolver;

public class CachingModelResolverDecoratorTest {

	private TestCachingModelResolverDecorator modelResolver;
	private ModelResolver innerModelResolver;

	private final Object object = new Object();
	private final String objectType = "Object";
	private final Class<Object> objectClass = Object.class;
	private final String idField = "id";
	
	@Before
	public void initializeModelResolver() {
		this.innerModelResolver = Mockito.mock(ModelResolver.class);
		Mockito.doReturn(objectClass).when(innerModelResolver).resolveType(objectType);
		Mockito.doReturn(idField).when(innerModelResolver).getContextPath(objectType, objectType);
		Mockito.doReturn(objectClass).when(innerModelResolver).resolveType(object);
		this.modelResolver = new TestCachingModelResolverDecorator(this.innerModelResolver);
		this.modelResolver.clearCaches();
	}
	
	@Test
	public void test_caching_type_resolution_by_type__inner_method_called_once() {
		Class<?> actual1 = this.modelResolver.resolveType(objectType);
		Class<?> actual2 = this.modelResolver.resolveType(objectType);
		
		assertEquals(objectClass, actual1);
		assertEquals(actual1, actual2);
		
		verify(this.innerModelResolver, times(1)).resolveType(objectType);
	}

	@Test
	public void test_caching_type_resolution_by_class__inner_method_called_once() {
		Class<?> actual1 = this.modelResolver.resolveType(object);
		Class<?> actual2 = this.modelResolver.resolveType(object);

		assertEquals(objectClass, actual1);
		assertEquals(actual1, actual2);

		verify(this.innerModelResolver, times(1)).resolveType(object);
	}

	@Test
	public void test_caching_context_path__inner_method_called_once() {
		Object actual1 = this.modelResolver.getContextPath(objectType, objectType);
		Object actual2 = this.modelResolver.getContextPath(objectType, objectType);

		assertTrue(actual1 instanceof String);
		assertTrue(actual2 instanceof String);

		assertEquals(idField, actual1);
		assertEquals(actual1, actual2);

		verify(this.innerModelResolver, times(1)).getContextPath(objectType, objectType);
	}
}