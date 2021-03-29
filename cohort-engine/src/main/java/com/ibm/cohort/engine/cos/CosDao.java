/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;

public class CosDao {

	private static final Logger logger = LoggerFactory.getLogger(CosDao.class);

	private final AmazonS3 cosClient;

	CosDao(AmazonS3 cosClient) {
		this.cosClient = cosClient;
	}

	public String getObjectAsString(String bucket, String key) throws IOException {
		logger.trace("Retrieving object from bucket `{}` with key: `{}`", bucket, key);
		try (
				S3Object object = cosClient.getObject(new GetObjectRequest(bucket, key));
				InputStream stream = object.getObjectContent()
		) {
			return IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			logger.error("Error reading file: `{}`", key, e);
			throw e;
		}
	}

	public void createBucket(String bucket) {
		logger.trace("Creating COS bucket: {}", bucket);
		cosClient.createBucket(bucket);
	}

	public void deleteBucket(String bucket) {
		logger.trace("Deleting COS bucket: {}", bucket);
		cosClient.deleteBucket(bucket);
	}

	public List<String> getBuckets() {
		logger.trace("Retrieving all COS buckets");
		List<Bucket> bucketList = cosClient.listBuckets();

		return bucketList.stream().map(Bucket::getName).collect(Collectors.toList());
	}

	public void deleteObject(String bucket, String key) {
		logger.trace("Deleting COS object in {}: {}", bucket, key);
		cosClient.deleteObject(bucket, key);
	}

	public void putObject(String bucket, String key, String content) {
		logger.trace("Putting COS object into {}: {}", bucket, key);
		cosClient.putObject(bucket, key, content);
	}
}
