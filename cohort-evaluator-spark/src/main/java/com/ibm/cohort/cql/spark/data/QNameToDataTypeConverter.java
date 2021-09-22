/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.data;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

public class QNameToDataTypeConverter {
	
	private QNameToDataTypeConverter() {
		
	}

	public static final String ELM_NAMESPACE_URI = "urn:hl7-org:elm-types:r1";
	
	private static Map<QName, DataType> qNameToDataType = new HashMap<>();
	
	static {
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "Boolean"), DataTypes.BooleanType);
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "Integer"), DataTypes.IntegerType);
		// TODO: Revisit decimal precision. Possibly force configuration.
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "Decimal"), DataTypes.createDecimalType(28, 8));
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "String"), DataTypes.StringType);
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "Long"), DataTypes.LongType);
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "Date"), DataTypes.DateType);
		qNameToDataType.put(new QName(ELM_NAMESPACE_URI, "DateTime"), DataTypes.TimestampType);
	}
	
	public static DataType getFieldType(QName qName) {
		DataType dataType = null;
		
		if (qName != null) {
			dataType = qNameToDataType.get(qName);
		}

		if (dataType == null) {
			throw new UnsupportedOperationException("Writing out results of type " + qName + " is not currently supported.");
		}
		
		return dataType;
	}
	
	public static QName createQNameForElmNamespace(String localType) {
		if (localType == null) {
			throw new IllegalArgumentException("Cannot create QName from a null String.");
		}
		return new QName(ELM_NAMESPACE_URI, localType);
	}
}