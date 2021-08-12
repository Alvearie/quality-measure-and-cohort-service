/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.datarow.exception.UnsupportedConversionException;

public class SparkTypeConverterTest {
    private SparkTypeConverter typeConverter;
    
    @Before
    public void setUp() {
        typeConverter = new SparkTypeConverter(true);
    }
    
    @Test
    public void testLongOutOfIntMaxRangeIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlLong( Long.valueOf(Integer.MAX_VALUE) + 1) );
    }
    
    @Test
    public void testLongOutOfIntMinRangeIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlLong( Long.valueOf(Integer.MIN_VALUE) - 1) );
    }
    
    @Test
    public void testJavaUtilDateIsUnsupportedDate() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlDate( new java.util.Date() ));
    }
    
    @Test
    public void testJavaUtilDateIsUnsupportedDateTime() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlDateTime( new java.util.Date() ));
    }
    
    @Test
    public void testCqlTupleIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlTuple( null ));
    }

    @Test
    public void testCqlListIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlList( null ));
    }
    
    @Test
    public void testCqlBinaryIsUnsupported() {
        assertThrows(UnsupportedConversionException.class, () -> typeConverter.toCqlBinary( null ));
    }
    
}
