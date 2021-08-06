/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.ibm.cohort.datarow.exception.UnsupportedConversionException;

public class SparkTypeConverterTest {
    @Test
    public void testLongOutOfIntMaxRangeIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlLong( Long.valueOf(Integer.MAX_VALUE) + 1) );
    }
    
    @Test
    public void testLongOutOfIntMinRangeIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlLong( Long.valueOf(Integer.MIN_VALUE) - 1) );
    }
    
    @Test
    public void testJavaUtilDateIsUnsupportedDate() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlDate( new java.util.Date() ));
    }
    
    @Test
    public void testJavaUtilDateIsUnsupportedDateTime() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlDateTime( new java.util.Date() ));
    }
    
    @Test
    public void testCqlTupleIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlTuple( null ));
    }

    @Test
    public void testCqlListIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlList( null ));
    }
    
    @Test
    public void testCqlBinaryIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> SparkTypeConverter.toCqlBinary( null ));
    }
}
