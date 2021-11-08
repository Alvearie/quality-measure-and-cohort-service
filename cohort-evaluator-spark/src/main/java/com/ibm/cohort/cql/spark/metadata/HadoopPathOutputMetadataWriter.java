/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.metadata;


import java.io.IOException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class HadoopPathOutputMetadataWriter extends BaseOutputMetadataWriter {
    
    private final Path metadataPath;
    private final Configuration hadoopConfig;
    
    public HadoopPathOutputMetadataWriter(Path metadataPath, Configuration hadoopConfig) {
        this.metadataPath = metadataPath;
        this.hadoopConfig = hadoopConfig;
    }
    
    @Override
    protected boolean isSuccessfulRun(EvaluationSummary evaluationSummary) {
        return CollectionUtils.isEmpty(evaluationSummary.getErrorList());
    }
    
    @Override
    protected void createSuccessMarker() {
        try(FSDataOutputStream outputStream = metadataPath.getFileSystem(hadoopConfig).create(metadataPath.suffix("/" + SUCCESS_MARKER))) {
        } catch (IOException e) {
            throw new RuntimeException("Error writing the success marker file", e);
        }
    }
    
    @Override
    protected void writeBatchSummary(EvaluationSummary evaluationSummary) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try (FSDataOutputStream outputStream = metadataPath.getFileSystem(hadoopConfig).create(metadataPath.suffix("/" + BATCH_SUMMARY_PREFIX + evaluationSummary.getApplicationId()))) {
            String jsonString = writer.writeValueAsString(evaluationSummary);
            outputStream.write(jsonString.getBytes());
        } catch (IOException e) {
			throw new RuntimeException("Error writing batch summary report", e);
		}
	}
}
