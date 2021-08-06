/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

import java.util.Map;

public class AWSClientConfigFactory {
    public static AWSClientConfig fromEnvironment() {
        return fromMap(System.getenv());
    }

    public static AWSClientConfig fromMap(Map<String, String> map) {
        return new AWSClientConfig().setAwsAccessKey(map.get(EnvConstants.AWS_ACCESS_KEY_KEY))
                .setAwsSecretKey(map.get(EnvConstants.AWS_SECRET_KEY_KEY))
                .setAwsEndpoint(map.get(EnvConstants.AWS_ENDPOINT_KEY))
                .setAwsLocation(map.get(EnvConstants.AWS_LOCATION_KEY));
    }
}