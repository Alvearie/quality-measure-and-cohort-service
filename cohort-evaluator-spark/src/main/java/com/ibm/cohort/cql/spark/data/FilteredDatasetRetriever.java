/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.util.StringMatcher;

public class FilteredDatasetRetriever implements DatasetRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(FilteredDatasetRetriever.class);
    
    private DatasetRetriever retriever;
    private Map<String, Set<StringMatcher>> dataTypeFilters;

    public FilteredDatasetRetriever(DatasetRetriever retriever, Map<String,Set<StringMatcher>> filters) {
        this.retriever = retriever;
        this.dataTypeFilters = filters;
    }

    @Override
    public Dataset<Row> readDataset(String dataType, String path) {
        LOG.info("Reading dataset {} from {} with filters {}", dataType, path, dataTypeFilters);
        
        Dataset<Row> result = null;
        
        Collection<StringMatcher> columnNameMatchers = dataTypeFilters.get(dataType);
        if( CollectionUtils.isNotEmpty(columnNameMatchers) ) {
            Dataset<Row> sourceDataset = retriever.readDataset(dataType, path);
            
            List<Column> cols = new ArrayList<>();
            for( StringMatcher colNameMatcher : columnNameMatchers ) {
                try {
                    Stream.of(sourceDataset.schema().fieldNames())
                        .filter( fn -> colNameMatcher.test(fn) )
                        .map( fn -> sourceDataset.col( fn ) )
                        .forEach( col -> {
                            cols.add(col);
                            
                            Metadata metadata = MetadataUtils.getColumnMetadata(sourceDataset.schema(), col.toString());
                            if( metadata != null ) {
                                if( MetadataUtils.isCodeCol(metadata) ) {
                                    String systemCol = MetadataUtils.getSystemCol(metadata);
                                    if( systemCol != null ) {
                                        cols.add( sourceDataset.col(systemCol) );
                                    }
        
                                    String displayCol = MetadataUtils.getDisplayCol(metadata);
                                    if( displayCol != null ) {
                                        cols.add( sourceDataset.col(displayCol) );
                                    }
                                }
                            }
                        });
                } catch( Throwable th ) {
                    LOG.error("Failed to resolve column %s of data type %s", th);
                    throw th;
                }
            }
            result = sourceDataset.select( cols.toArray(new Column[cols.size()]) );
        }
        
        return result;
    }
}
