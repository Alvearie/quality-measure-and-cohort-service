package com.ibm.cohort.cql.spark.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.cql.util.StringMatcher;

public class ColumnFilterFunction implements Function<Dataset<Row>, Dataset<Row>> {
	private static final Logger LOG = LoggerFactory.getLogger(ColumnFilterFunction.class);
	
	private final Set<StringMatcher> columnNameMatchers;
	
	public ColumnFilterFunction(Set<StringMatcher> columnNameMatchers) {
		this.columnNameMatchers = columnNameMatchers;
	}
	
	/**
	 * Filter the columns in a dataset based on a set of matching rules provided at class initialization.
	 * 
	 * @param input Dataset to be filtered
	 * @return Dataset filtered to the columns matching one or more string matchers used to initialize
	 *         this class. If a code column is included in the output, any columns associated with the
	 *         code column through metadata fields will also be included.
	 */
	@Override
	public Dataset<Row> apply(Dataset<Row> input) {
		Dataset<Row> result = null;

		if( CollectionUtils.isNotEmpty(columnNameMatchers) ) {
			Dataset<Row> sourceDataset = input;

			List<Column> cols = new ArrayList<>();
			for( StringMatcher colNameMatcher : columnNameMatchers) {
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
			result = sourceDataset.select( cols.toArray(new Column[0]) );
		}

		return result;
	}
}
