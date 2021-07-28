/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.elm.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class OptimizedObjectFactoryTest {

	@Test
	public void ensureAllCreateMethodsOverridden() {
		Class<?> factory = OptimizedObjectFactory.class;
		Method[] subclassMethods = factory.getDeclaredMethods();
		Method[] superclassMethods = factory.getSuperclass().getDeclaredMethods();

		Set<String> subclassCreates = getCreates(subclassMethods);
		Set<String> superclassCreates = getCreates(superclassMethods);

		assertThat(subclassCreates, is(superclassCreates));
		// any missing overrides from the superclass must be redundantly overridden
		// due to how moxy's annotations processor handles object factories
	}

	private Set<String> getCreates(Method[] methods) {
		return Arrays.stream(methods)
				.filter(method -> method.getName().startsWith("create"))
				.map(Method::getReturnType)
				.map(Class::getName)
				.collect(Collectors.toSet());
	}
}