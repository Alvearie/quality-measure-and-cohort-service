/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

import java.util.function.Consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWSClientHelpers {
    
    public static String toS3Url(String bucket, String inputPath, String filename) {
        return String.format(
                "s3a://%s/%s/%s",
                bucket,
                inputPath,
                filename
        );
    }
    
    public static void processS3Objects(AmazonS3 s3client, String bucket, String keyPrefix, ObjectConsumer consumer) {
        processS3ObjectKeys(s3client, bucket, keyPrefix, osm -> {
            String obj = s3client.getObjectAsString(bucket, osm.getKey());
            consumer.consume(osm,obj);
        });
    }
    
    public static void processS3ObjectKeys(AmazonS3 s3client, String bucket, String keyPrefix, Consumer<S3ObjectSummary> consumer) {
        //https://docs.aws.amazon.com/AmazonS3/latest/userguide/ListingKeysUsingAPIs.html
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(keyPrefix);
        ListObjectsV2Result result;
        do {
            result = s3client.listObjectsV2(req);
            result.getObjectSummaries().forEach( osm -> {
                consumer.accept(osm);
            });
            
            String token = result.getContinuationToken();
            req.setContinuationToken(token);
        } while( result.isTruncated() );
    }
}
