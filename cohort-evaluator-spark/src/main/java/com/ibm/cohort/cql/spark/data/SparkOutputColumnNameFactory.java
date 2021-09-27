package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;

public class SparkOutputColumnNameFactory implements Serializable {
	
	private final String columnDelimieter;
	
	public SparkOutputColumnNameFactory(String columnDelimieter) {
		this.columnDelimieter = columnDelimieter;
	}
	
	public String getColumnName(String libraryId, String defineName) {
		return String.join(columnDelimieter, libraryId, defineName);
	}
}
