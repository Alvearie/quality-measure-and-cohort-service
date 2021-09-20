/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class AWSClientConfigFactoryTest {
    @Test
    public void testConfigFromMap() {

        String expectedAccess = "access";
        String expectedSecret = "secret";
        String expectedEndpoint = "endpoint";
        String expectedLocation = "location";

        Map<String, String> env = new HashMap<>();
        env.put(EnvConstants.AWS_ACCESS_KEY_KEY, expectedAccess);
        env.put(EnvConstants.AWS_SECRET_KEY_KEY, expectedSecret);
        env.put(EnvConstants.AWS_ENDPOINT_KEY, expectedEndpoint);
        env.put(EnvConstants.AWS_LOCATION_KEY, expectedLocation);

        AWSClientConfig config = AWSClientConfigFactory.fromMap(env);
        assertEquals(expectedAccess, config.getAwsAccessKey());
        assertEquals(expectedSecret, config.getAwsSecretKey());
        assertEquals(expectedEndpoint, config.getAwsEndpoint());
        assertEquals(expectedLocation, config.getAwsLocation());
    }
    
    public void testConfigFromEnvironment() {

        String expectedAccess = "access";
        String expectedSecret = "secret";
        String expectedEndpoint = "endpoint";
        String expectedLocation = "location";

        System.setProperty(EnvConstants.AWS_ACCESS_KEY_KEY, expectedAccess);
        System.setProperty(EnvConstants.AWS_SECRET_KEY_KEY, expectedSecret);
        System.setProperty(EnvConstants.AWS_ENDPOINT_KEY, expectedEndpoint);
        System.setProperty(EnvConstants.AWS_LOCATION_KEY, expectedLocation);

        AWSClientConfig config = AWSClientConfigFactory.fromEnvironment();
        assertEquals(expectedAccess, config.getAwsAccessKey());
        assertEquals(expectedSecret, config.getAwsSecretKey());
        assertEquals(expectedEndpoint, config.getAwsEndpoint());
        assertEquals(expectedLocation, config.getAwsLocation());
    }
}
