package com.ibm.cohort.cql.aws;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AWSClientConfigTest {
    @Test
    public void testSetterGetter() {
        String expectedAccess = "access";
        String expectedSecret = "secret";
        String expectedEndpoint = "endpoint";
        String expectedLocation = "location";

        AWSClientConfig config = new AWSClientConfig();
        config.setAwsAccessKey(expectedAccess);
        config.setAwsSecretKey(expectedSecret);
        config.setAwsEndpoint(expectedEndpoint);
        config.setAwsLocation(expectedLocation);

        assertEquals(expectedAccess, config.getAwsAccessKey());
        assertEquals(expectedSecret, config.getAwsSecretKey());
        assertEquals(expectedEndpoint, config.getAwsEndpoint());
        assertEquals(expectedLocation, config.getAwsLocation());
    }
}
