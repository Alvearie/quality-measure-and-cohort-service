package com.ibm.cohort.cql.spark.data;

public interface SparkOutputColumnEncoder {
	String getColumnName(String libraryId, String defineName);
}
