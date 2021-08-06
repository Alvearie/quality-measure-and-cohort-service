package com.ibm.cohort.cql.aws;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;

public class AWSClientFactoryTest {
    @Test
    public void testCreateClientSuccess() {
        String expectedAccess = "access";
        String expectedSecret = "secret";
        String expectedEndpoint = "endpoint";
        String expectedLocation = "location";

        AWSClientConfig config = new AWSClientConfig();
        config.setAwsAccessKey(expectedAccess);
        config.setAwsSecretKey(expectedSecret);
        config.setAwsEndpoint(expectedEndpoint);
        config.setAwsLocation(expectedLocation);
        
        AmazonS3 client = AWSClientFactory.getInstance().createClient(config);
        assertNotNull(client);
        // not much else we can test without valid credentials
    }
}
