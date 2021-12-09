/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;


import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.Test;

public class QNameToDataTypeConverterTest {

	@Test
	public void testCreateElmQName() {
		String localValue = "val";
		QName qName = QNameToDataTypeConverter.createQNameForElmNamespace(localValue);
		
		assertEquals(new QName(QNameToDataTypeConverter.ELM_NAMESPACE_URI, localValue), qName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateElmQNameNullString() {
		QName qName = QNameToDataTypeConverter.createQNameForElmNamespace(null);

		assertEquals(new QName(QNameToDataTypeConverter.ELM_NAMESPACE_URI, ""), qName);
	}
	
	@Test
	public void testQNameConverterForSupportedType() {
		DataType dataType = QNameToDataTypeConverter.getFieldType(new QName(QNameToDataTypeConverter.ELM_NAMESPACE_URI, "String"));
		assertEquals(DataTypes.StringType, dataType);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testQNameConverterForUnsupportedType() {
		QNameToDataTypeConverter.getFieldType(new QName("bad", "type"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testQNameConverterForNullQName() {
		QNameToDataTypeConverter.getFieldType(null);
	}
}