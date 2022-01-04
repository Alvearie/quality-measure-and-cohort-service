/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;


import org.apache.commons.collections.CollectionUtils;

public abstract class BaseOutputMetadataWriter implements OutputMetadataWriter{
    
    public static final String SUCCESS_MARKER = "_SUCCESS";
    public static final String SUCCESS_FLAG = "SUCCESS";
    public static final String BATCH_SUMMARY_PREFIX = "batch_summary-";

    @Override
    public void writeMetadata(EvaluationSummary evaluationSummary) {
        if (isSuccessfulRun(evaluationSummary)) {
            createSuccessMarker();
        }
        writeBatchSummary(evaluationSummary);
    }
    
    protected boolean isSuccessfulRun(EvaluationSummary evaluationSummary) {
        return CollectionUtils.isEmpty(evaluationSummary.getErrorList());
    }
    
    protected abstract void createSuccessMarker();
    protected abstract void writeBatchSummary(EvaluationSummary evaluationSummary);
}
