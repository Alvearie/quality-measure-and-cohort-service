/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cos;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;

public class CosDaoFactory {

	private CosDaoFactory() {
	}

	public static CosDao from(CosConfiguration configuration) {
		AmazonS3 cosClient = createClient(
				configuration.getApiKey(),
				configuration.getServiceInstanceId(),
				configuration.getEndpointUrl(),
				configuration.getLocation()
		);

		return new CosDao(cosClient);
	}

	private static AmazonS3 createClient(String apiKey, String serviceInstanceId, String endpointUrl, String location) {
		AWSCredentials credentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceId);
		ClientConfiguration clientConfig = new ClientConfiguration()
				.withRequestTimeout(5_000)
				.withTcpKeepAlive(true);

		return AmazonS3ClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, location))
				.withPathStyleAccessEnabled(true)
				.withClientConfiguration(clientConfig)
				.build();
	}
}
