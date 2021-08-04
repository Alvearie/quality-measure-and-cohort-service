/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

public class AWSClientConfig {
    public String awsSecretKey;
    public String awsAccessKey;
    public String awsEndpoint;
    public String awsLocation;

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public AWSClientConfig setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
        return this;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public AWSClientConfig setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
        return this;
    }

    public String getAwsEndpoint() {
        return awsEndpoint;
    }

    public AWSClientConfig setAwsEndpoint(String awsEndpoint) {
        this.awsEndpoint = awsEndpoint;
        return this;
    }

    public String getAwsLocation() {
        return awsLocation;
    }

    public AWSClientConfig setAwsLocation(String awsLocation) {
        this.awsLocation = awsLocation;
        return this;
    }
}
