package com.ibm.cohort.cql.aws;

import com.amazonaws.services.s3.model.S3ObjectSummary;

@FunctionalInterface
public interface ObjectConsumer {
    public void consume(S3ObjectSummary osm, String contents);
}
