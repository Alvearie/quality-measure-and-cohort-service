/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.spark.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;

import com.ibm.cohort.datarow.exception.UnsupportedConversionException;

/**
 * SparkTypeConverter provides methods for converting from the Java 
 * types produced by the Spark SQL Row API into the CQL typesystem.
 * The general approach is to pass through raw Java types where 
 * possible, convert whole numbers to Integer, and convert real numbers 
 * into java.math.BigDecimal. Dates are converted into the CQL engine
 * runtime types. Binary and compound data structures (map, list, row)
 * are unsupported at this time.
 */
public class SparkTypeConverter {
	
	public static Object toCqlBoolean(Object obj) {
		return obj;
	}
	
	public static Object toCqlString(Object obj) {
		return obj;
	}
    
	public static Object toCqlByte(Object obj) {
    	return ((Byte)obj).intValue();
    }
    
    public static Object toCqlShort(Object obj) {
    	return ((Short)obj).intValue();
    }
    
	public static Object toCqlInteger(Object obj) {
		return obj;
	}
    
    public static Object toCqlLong(Object sparkVal) {
    	Object result;
    	
    	Long l = (Long) sparkVal;
    	if( Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE ) {
    		result = (int) l.longValue();
    	} else {
    		//There is a PR open in the CQL engine for adding Long support. Revisit this when that PR is completed.
    		throw new UnsupportedConversionException(String.format("Long value '%s' is outside the range of integer", l.toString()));
    	}
    	
    	return result;
    }
    
    public static Object toCqlFloat(Object obj) {
    	return BigDecimal.valueOf((float)obj);
    }
    
    public static Object toCqlDouble(Object obj) {
    	return BigDecimal.valueOf((double)obj);
    }
    
    public static Object toCqlDecimal(Object obj) {
    	return obj;
    }
    
    public static Object toCqlDate(Object obj) {
    	if( obj instanceof LocalDate ) {
    		return localDateToDate((LocalDate) obj);
    	} else if( obj instanceof java.sql.Date ) {
    		return localDateToDate(((java.sql.Date)obj).toLocalDate());
    	} else { 
    		throw new UnsupportedConversionException("Unexpected date type " + obj.getClass().getName());
    	}
    }
    
    public static Object toCqlDateTime(Object obj) {
    	if( obj instanceof Instant ) {
    		return instantToDateTime((Instant) obj);
    	} else if( obj instanceof java.sql.Timestamp ) {
    		return instantToDateTime(((java.sql.Timestamp)obj).toInstant());
    	} else { 
    		throw new UnsupportedConversionException("Unexpected date type " + obj.getClass().getName());
    	}
    }
    
    public static Object toCqlList(Object obj) { 
    	convertUnhandledValue(obj);
    	return obj;
    }
    
    public static Object toCqlTuple(Object obj) { 
    	convertUnhandledValue(obj);
    	return obj;
    }
    
    public static Object toCqlBinary(Object obj) { 
    	convertUnhandledValue(obj);
    	return obj;
    }
    
	public static void convertUnhandledValue(Object sparkVal) {
		throw new UnsupportedConversionException(String.format("Type %s is not supported", (sparkVal != null ) ? sparkVal.getClass().getName() : null));
	}

	protected static Date localDateToDate(LocalDate localDate) {
		return new Date(localDate);
	}

	protected static DateTime instantToDateTime(Instant instant) {
		return new DateTime(instant.atZone(getZoneOffset()).toOffsetDateTime());
	}

	protected static ZoneId getZoneOffset() {
    	//TODO : use GMT or pull the value from some configuration (e.g. Context)
		return ZoneOffset.systemDefault();
	}
}
