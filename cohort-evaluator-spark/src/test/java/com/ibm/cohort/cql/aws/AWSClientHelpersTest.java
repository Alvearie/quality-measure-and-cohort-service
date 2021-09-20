/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.aws;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AWSClientHelpersTest {
    @Test
    public void testToS3Url() {
        assertEquals("s3a://bucket/path/file", AWSClientHelpers.toS3Url("bucket", "path", "file"));
    }
}
