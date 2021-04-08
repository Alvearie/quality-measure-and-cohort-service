/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cache.cos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream;

@RunWith(MockitoJUnitRunner.class)
public class CosDaoTest {

	private static final String TEST_BUCKET = "test-bucket";
	private static final String TEST_KEY = "test-key";

	@Mock
	private AmazonS3 cosClient;

	private CosDao dao;

	@Before
	public void setUp() {
		dao = new CosDao(cosClient);
	}

	@Test
	public void getObjectAsString() throws IOException {
		S3Object object = mock(S3Object.class);
		String expected = "ohhi";
		doReturn(new S3ObjectInputStream(IOUtils.toInputStream(expected, StandardCharsets.UTF_8), null))
				.when(object)
				.getObjectContent();
		doReturn(object)
				.when(cosClient)
				.getObject(any());

		String actual = dao.getObjectAsString(TEST_BUCKET, TEST_KEY);
		assertEquals(actual, expected);
	}

	@Test
	public void createBucket() {
		String bucket = TEST_BUCKET;
		dao.createBucket(bucket);

		verify(cosClient).createBucket(eq(bucket));
	}

	@Test
	public void deleteBucket() {
		String bucket = TEST_BUCKET;
		dao.deleteBucket(bucket);

		verify(cosClient).deleteBucket(eq(bucket));
	}

	@Test
	public void getBuckets() {
		List<Bucket> buckets = new ArrayList<>();
		buckets.add(new Bucket("a"));
		buckets.add(new Bucket("b"));
		buckets.add(new Bucket("c"));
		doReturn(buckets).when(cosClient).listBuckets();

		List<String> actual = dao.getBuckets();

		assertThat(actual, contains("a", "b", "c"));
	}

	@Test
	public void deleteObject() {
		String bucket = TEST_BUCKET;
		String key = TEST_KEY;
		dao.deleteObject(bucket, key);

		verify(cosClient).deleteObject(eq(bucket), eq(key));
	}

	@Test
	public void putObject() {
		String bucket = TEST_BUCKET;
		String key = TEST_KEY;
		String content = "ohhi";
		dao.putObject(bucket, key, content);

		verify(cosClient).putObject(eq(bucket), eq(key), eq(content));
	}
}