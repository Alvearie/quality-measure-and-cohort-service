/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.cache;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.ibm.cohort.cql.cache.DefaultRetrieveCacheContext;
import com.ibm.cohort.cql.cache.RetrieveCacheContext;
import com.ibm.cohort.cql.cache.RetrieveCacheKey;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.cache.Cache;

public class DefaultRetrieveCacheContextTest {

	@Test
	public void getCache_withConcreteImplementation() throws Exception {
		CaffeineConfiguration<RetrieveCacheKey, Iterable<Object>> config = new CaffeineConfiguration<>();
		try(RetrieveCacheContext context = new DefaultRetrieveCacheContext(config)) {
			Cache<RetrieveCacheKey, Iterable<Object>> cache = context.getCache("1");
			Assert.assertNotNull(cache);
		}
	}

	@Test
	public void getCache_sameContextId_singleCall() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));

		Mockito.verify(expected, Mockito.times(0)).clear();
	}

	@Test
	public void getCache_sameContextId_multipleCall() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		for (int i = 0; i < 10; i++) {
			Assert.assertSame(expected, context.getCache("1"));
		}

		Mockito.verify(expected, Mockito.times(0)).clear();
	}

	@Test
	public void getCache_differentContextId_singleCall() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("2"));

		Mockito.verify(expected, Mockito.times(1)).clear();
	}

	@Test
	public void getCache_differentContextId_multipleCall() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		int numIterations = 10;
		for (int i = 0; i < numIterations; i++) {
			Assert.assertSame(expected, context.getCache(Integer.toString(i)));
		}

		Mockito.verify(expected, Mockito.times(numIterations - 1)).clear();
	}

	@Test
	public void getCache_mixedCase() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("1"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("2"));
		Assert.assertSame(expected, context.getCache("3"));
		Assert.assertSame(expected, context.getCache("3"));
		Assert.assertSame(expected, context.getCache("4"));
		Assert.assertSame(expected, context.getCache("5"));

		Mockito.verify(expected, Mockito.times(4)).clear();
	}

	@Test
	public void flushCache() {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		context.flushCache();

		Mockito.verify(expected, Mockito.times(1)).clear();
	}

	@Test
	public void close() throws Exception {
		Cache<RetrieveCacheKey, Iterable<Object>> expected = Mockito.mock(Cache.class);

		RetrieveCacheContext context = new DefaultRetrieveCacheContext(expected);
		context.close();

		Mockito.verify(expected, Mockito.times(1)).clear();
		Mockito.verify(expected, Mockito.times(1)).close();
	}

}
