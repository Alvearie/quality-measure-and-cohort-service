/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteredDatasetRetriever implements DatasetRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(FilteredDatasetRetriever.class);
    
    private DatasetRetriever retriever;
    private Map<String, Set<String>> dataTypeFilters;

    public FilteredDatasetRetriever(DatasetRetriever retriever, Map<String,Set<String>> dataTypeFilters) {
        this.retriever = retriever;
        this.dataTypeFilters = dataTypeFilters;
    }

    @Override
    public Dataset<Row> readDataset(String dataType, String path) {
        Dataset<Row> result = null;
        
        Set<String> columnNames = dataTypeFilters.get(dataType);
        if( columnNames.size() > 0 ) {
            result = retriever.readDataset(dataType, path);
            
            List<Column> cols = new ArrayList<>();//Column[ columnNames.size() ];
            for( String colName : columnNames ) {
                try {
                    Column col = result.col(colName);
                    cols.add(col);
                    
                    Metadata metadata = MetadataUtils.getColumnMetadata(result.schema(), colName);
                    if( metadata != null ) {
                        if( MetadataUtils.isCodeCol(metadata) ) {
                            String systemCol = MetadataUtils.getSystemCol(metadata);
                            if( systemCol != null ) {
                                cols.add( result.col(systemCol) );
                            }

                            String displayCol = MetadataUtils.getDisplayCol(metadata);
                            if( displayCol != null ) {
                                cols.add( result.col(displayCol) );
                            }
                        }
                    }
                } catch( Throwable th ) {
                    LOG.error("Failed to resolve column %s of data type %s", th);
                    throw th;
                }
            }
            result = result.select( cols.toArray(new Column[cols.size()]) );
        }
        
        return result;
    }
}
