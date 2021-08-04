package com.ibm.cohort.cql.aws;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class AWSClientConfigFactoryTest {
    @Test
    public void testConfigFromEnvironment() {

        String expectedAccess = "access";
        String expectedSecret = "secret";
        String expectedEndpoint = "endpoint";
        String expectedLocation = "location";

        Map<String, String> env = new HashMap<>();
        env.put(AWSClientConfigFactory.AWS_ACCESS_KEY_KEY, expectedAccess);
        env.put(AWSClientConfigFactory.AWS_SECRET_KEY_KEY, expectedSecret);
        env.put(AWSClientConfigFactory.AWS_ENDPOINT_KEY, expectedEndpoint);
        env.put(AWSClientConfigFactory.AWS_LOCATION_KEY, expectedLocation);

        AWSClientConfig config = AWSClientConfigFactory.fromMap(env);
        assertEquals(expectedAccess, config.getAwsAccessKey());
        assertEquals(expectedSecret, config.getAwsSecretKey());
        assertEquals(expectedEndpoint, config.getAwsEndpoint());
        assertEquals(expectedLocation, config.getAwsLocation());
    }
}
