/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

public class AWSClientHelpers {
    public static String toS3Url(String bucket, String inputPath, String filename) {
        return String.format(
                "s3a://%s/%s/%s",
                bucket,
                inputPath,
                filename
        );
    }
}
