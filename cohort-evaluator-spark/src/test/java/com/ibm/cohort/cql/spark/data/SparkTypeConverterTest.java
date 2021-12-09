/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Precision;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Tuple;

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
    
    @Test
    public void testToSparkDate() {
        LocalDate expected = LocalDate.of(2000, 07, 04);
        org.opencds.cqf.cql.engine.runtime.Date date = new org.opencds.cqf.cql.engine.runtime.Date(expected);
        
        Object converted = typeConverter.toSparkDate(date);
        assertTrue(converted instanceof LocalDate);
        assertEquals(expected, (LocalDate) converted);
    }
    
    @Test
    public void testToSparkDateTime() {
        LocalTime expectedTime = LocalTime.of(12,0,0,0);
        LocalDate expectedDate = LocalDate.of(2000, 07, 04);
        OffsetDateTime expectedDateTime = OffsetDateTime.of(expectedDate, expectedTime, ZoneOffset.UTC);
        org.opencds.cqf.cql.engine.runtime.DateTime dateTime = new org.opencds.cqf.cql.engine.runtime.DateTime(expectedDateTime);
        
        Object converted = typeConverter.toSparkDateTime(dateTime);
        assertTrue(converted instanceof java.time.Instant);
        //TODO - There is a millisecond discrepancy that needs to be figured out.
        //assertEquals(expectedDateTime, (java.time.Instant) converted);
    }
    
    @Test
    public void testToSparkTuple() {
        LinkedHashMap<String,Object> elements = new LinkedHashMap<>();
        elements.put("String", "Hello,World");
        elements.put("Integer", 10);
        
        Tuple tuple = new Tuple();
        tuple.setElements(elements);
        
        Object converted = typeConverter.toSparkTuple(tuple);
        assertTrue(converted instanceof scala.collection.Map);
    }
    
    @Test
    public void testToSparkRatio() {
        Ratio ratio = new Ratio()
                .setNumerator(new Quantity().withValue(new BigDecimal(1.2)).withUnit("mg/mL"))
                .setDenominator(new Quantity().withValue(new BigDecimal(5.9)).withUnit("mg/mL"));
        
        Object converted = typeConverter.toSparkRatio(ratio);
        assertTrue(converted instanceof scala.collection.Map);
    }
    
    @Test
    public void testToSparkInterval() {
        Interval interval = new Interval(10, true, 20, true);
                
        Object converted = typeConverter.toSparkInterval(interval);
        assertTrue(converted instanceof scala.collection.Map);
    }
    
    @Test
    public void testToSparkCode() {
        Code code = new Code().withCode("123").withSystem("MySystem").withDisplay("Display");
                
        Object converted = typeConverter.toSparkCode(code);
        assertTrue(converted instanceof scala.collection.Map);
    }
    
    @Test
    public void testToSparkInteger() {
        Integer expected = 100;
        Object actual = typeConverter.toSparkInteger(expected);
        assertEquals( expected, actual );
    }
    
    @Test
    public void testToSparkBoolean() {
        Boolean expected = Boolean.TRUE;
        Object actual = typeConverter.toSparkBoolean(expected);
        assertEquals( expected, actual );
    }
    
    @Test
    public void testToSparkLong() {
        Long expected = 10000000000l;
        Object actual = typeConverter.toSparkLong(expected);
        assertEquals( expected, actual );
    }
    
    @Test
    public void testToSparkFloat() {
        assertThrows( UnsupportedConversionException.class, () -> typeConverter.toSparkType(1.23f));
    }
    
    @Test
    public void testToSparkTime() {
        LocalTime expectedTime = LocalTime.of(12,0,0,0);

        org.opencds.cqf.cql.engine.runtime.Time time = new org.opencds.cqf.cql.engine.runtime.Time(expectedTime, Precision.MILLISECOND);
        
        Object actual = typeConverter.toSparkTime(time);
        assertTrue(actual instanceof scala.collection.Map);
    }
    
    @Test
    public void testToSparkList() {
        List<Object> list = new ArrayList<>();
        list.add(10);
        list.add(100000000l);
        list.add(true);
        list.add(5.1d);
        list.add( new BigDecimal(3.1415) );
        
        Object actual = typeConverter.toSparkList(list);
        assertEquals(list.size(), ((List<?>)actual).size());
    }
}
