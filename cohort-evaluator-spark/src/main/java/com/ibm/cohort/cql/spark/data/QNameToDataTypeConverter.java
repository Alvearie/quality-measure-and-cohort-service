package com.ibm.cohort.cql.spark.data;

import javax.xml.namespace.QName;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

public class QNameToDataTypeConverter {

	public static final String ELM_NAMESPACE_URI = "urn:hl7-org:elm-types:r1";
	
	public static DataType getFieldType(QName qName) {
		if (qName.equals(new QName(ELM_NAMESPACE_URI, "Boolean"))) {
			return DataTypes.BooleanType;
		}
		else if (qName.equals(new QName(ELM_NAMESPACE_URI, "Integer"))) {
			return DataTypes.IntegerType;
		}
		else if (qName.equals(new QName(ELM_NAMESPACE_URI, "Decimal"))) {
			// TODO: How do we actually want to handle this? We won't know anything about precision
			return DataTypes.createDecimalType(20, 5);
		}
		else if (qName.equals(new QName(ELM_NAMESPACE_URI, "String"))) {
			return DataTypes.StringType;
		}
		else if (qName.equals(new QName(ELM_NAMESPACE_URI, "Long"))) {
			return DataTypes.LongType;
		}
		else if(qName.equals(new QName(ELM_NAMESPACE_URI, "Date"))) {
			return DataTypes.DateType;
		}
		else if(qName.equals(new QName(ELM_NAMESPACE_URI, "DateTime"))) {
			return DataTypes.TimestampType;
		}
		else if (qName.equals(new QName(ELM_NAMESPACE_URI, "Any"))) {
			return DataTypes.ByteType;
		}
		else {
			throw new UnsupportedOperationException("Writing out results of type " + qName + " is not currently supported.");
		}
	}
}