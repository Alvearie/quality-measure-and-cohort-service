package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;

public class SparkOutputColumnNameFactory implements Serializable {
	
	private static final String DEFAULT_DELIMITER = "|";
	
	private final String columnDelimieter;
	
	public SparkOutputColumnNameFactory() {
		this(DEFAULT_DELIMITER);
	}
	
	public SparkOutputColumnNameFactory(String columnDelimieter) {
		this.columnDelimieter = columnDelimieter;
	}
	
	public String getColumnName(String libraryId, String defineName) {
		return String.join(columnDelimieter, libraryId, defineName);
	}
}
