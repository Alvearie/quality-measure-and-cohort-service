/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AWSClientFactory {

    private static final AWSClientFactory factory = new AWSClientFactory();
    
    public static AWSClientFactory getInstance() {
        return factory;
    }

    public AmazonS3 createClient(AWSClientConfig awsConfig) {
        AWSCredentials credentials = new BasicAWSCredentials(awsConfig.getAwsAccessKey(), awsConfig.getAwsSecretKey());
        ClientConfiguration clientConfig = new ClientConfiguration()
                .withRequestTimeout(50_000)
                .withTcpKeepAlive(true);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsConfig.getAwsEndpoint(), awsConfig.getAwsLocation()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();
    }

}