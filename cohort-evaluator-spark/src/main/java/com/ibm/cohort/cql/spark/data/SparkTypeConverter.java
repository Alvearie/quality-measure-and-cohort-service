/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.sql.Row;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Date;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Quantity;
import org.opencds.cqf.cql.engine.runtime.Ratio;
import org.opencds.cqf.cql.engine.runtime.Time;
import org.opencds.cqf.cql.engine.runtime.Tuple;

import com.ibm.cohort.datarow.exception.UnsupportedConversionException;

import scala.collection.JavaConverters;

/**
 * SparkTypeConverter provides methods for converting from the Java types
 * produced by the Spark SQL Row API into the CQL typesystem and vice versa. The
 * general approach is to pass through raw Java types where possible, convert
 * whole numbers to Integer, and convert real numbers into java.math.BigDecimal.
 * Dates are converted into the CQL engine runtime types. Binary and compound
 * data structures (map, list, row) are unsupported at this time. In the reverse,
 * Java types are again passed through untouched, CQL Date and DateTime runtime types
 * are converted back into java.time or java.sql equivalents and data structure
 * types are unhandled.
 */
public class SparkTypeConverter implements Serializable {
    private static final String SYSTEM_TYPE_PROPERTY = "__type";

    private static final long serialVersionUID = 1L;

    private boolean useJava8APIDatetime;
    
    public SparkTypeConverter(boolean useJava8APIDatetime) {
        this.useJava8APIDatetime = useJava8APIDatetime;
    }

    public Object toCqlBoolean(Object obj) {
        return obj;
    }

    public Object toCqlString(Object obj) {
        return obj;
    }

    public Object toCqlByte(Object obj) {
        return ((Byte) obj).intValue();
    }

    public Object toCqlShort(Object obj) {
        return ((Short) obj).intValue();
    }

    public Object toCqlInteger(Object obj) {
        return obj;
    }

    public Object toCqlLong(Object sparkVal) {
        Object result;

        Long l = (Long) sparkVal;
        if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
            result = (int) l.longValue();
        } else {
            // There is a PR open in the CQL engine for adding Long support. Revisit this
            // when that PR is completed.
            throw new UnsupportedConversionException(
                    String.format("Long value '%s' is outside the range of integer", l.toString()));
        }

        return result;
    }

    public Object toCqlFloat(Object obj) {
        return BigDecimal.valueOf((float) obj);
    }

    public Object toCqlDouble(Object obj) {
        return BigDecimal.valueOf((double) obj);
    }

    public Object toCqlDecimal(Object obj) {
        return obj;
    }

    public Object toCqlDate(Object obj) {
        if (obj instanceof LocalDate) {
            return localDateToDate((LocalDate) obj);
        } else if (obj instanceof java.sql.Date) {
            return localDateToDate(((java.sql.Date) obj).toLocalDate());
        } else {
            throw new UnsupportedConversionException("Unexpected date type " + obj.getClass().getName());
        }
    }

    public Object toCqlDateTime(Object obj) {
        if (obj instanceof Instant) {
            return instantToDateTime((Instant) obj);
        } else if (obj instanceof java.sql.Timestamp) {
            return instantToDateTime(((java.sql.Timestamp) obj).toInstant());
        } else {
            throw new UnsupportedConversionException("Unexpected date type " + obj.getClass().getName());
        }
    }

    public Object toCqlList(Object obj) {
        return convertUnhandledValue(obj);
    }

    public Object toCqlTuple(Object obj) {
        return convertUnhandledValue(obj);
    }

    public Object toCqlBinary(Object obj) {
        return convertUnhandledValue(obj);
    }
    
    public Object toCqlType(Object obj) { 
        Object result = null;
        if( obj instanceof Byte ) {
            result = toCqlByte(obj);
        } else if( obj instanceof Short ) {
            result = toCqlShort(obj);
        } else if( obj instanceof Integer ) {
            result = toCqlInteger(obj);
        } else if( obj instanceof Long ) {
            result = toCqlLong(obj);
        } else if( obj instanceof Float ) {
            result = toCqlFloat(obj);
        } else if( obj instanceof Double ) {
            result = toCqlDouble(obj);
        } else if( obj instanceof BigDecimal ) {
            result = toCqlDecimal(obj);
        } else if( obj instanceof String ) {
            result = toCqlString(obj);
        } else if( obj instanceof Boolean ) {
            result = toCqlBoolean(obj);
        } else if( obj instanceof java.sql.Date || obj instanceof LocalDate ) { 
            result = toCqlDate(obj);
        } else if( obj instanceof java.sql.Timestamp || obj instanceof Instant ) {
            result = toCqlDateTime(obj);
        } else if( obj instanceof scala.collection.Seq ) {
            result = toCqlList(obj);
        } else if( obj instanceof scala.collection.Map || obj instanceof Row ) {
            result = toCqlTuple(obj);
        } else if( obj instanceof byte[] ) {
            result = toCqlBinary(obj);
        } else {
            result = convertUnhandledValue(obj);
        }
        return result;
    }
    
    public Object toSparkBoolean(Object obj) {
        return obj;
    }
    
    public Object toSparkString(Object obj) {
        return obj;
    }
    
    public Object toSparkInteger(Object obj) {
        Integer result = null;
        if( obj != null ) {
            if( obj instanceof Number ) {
                result = ((Number) obj).intValue();
            }
        }
        return result;
    }
    
    public Object toSparkLong(Object obj) {
        Long result = null;
        if( obj != null ) {
            if( obj instanceof Number ) {
                result = ((Number) obj).longValue();
            }
        }
        return result;
    }
    
    public Object toSparkDecimal(Object obj) {
        BigDecimal result = null;
        if( obj != null ) {
            if( obj instanceof BigDecimal ) {
                result = (BigDecimal) obj;
            } else if( obj instanceof Number ) {
                result = new BigDecimal(((Number) obj).doubleValue());
            }
        }
        return result;
    }
    
    public Object toSparkDate(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Date ) {
                Date dt = (Date) obj;
                if( useJava8APIDatetime ) {
                    result = dt.getDate();
                } else { 
                    result = new java.sql.Date(dt.toJavaDate().getTime());
                }
            }
        }
        return result;
    }
    
    public Object toSparkDateTime(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof DateTime ) {
                DateTime dt = (DateTime) obj;
                if( useJava8APIDatetime ) {
                    result = dt.getDateTime().toInstant();
                } else { 
                    result = new java.sql.Timestamp(dt.toJavaDate().getTime());
                }
            }
        }
        return result;
    }
    
    public Object toSparkList(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof List ) {
                result = ((List<?>) obj).stream().map(this::toSparkType).collect(Collectors.toList());
            }
        }
        return result;
    }
    
    public Object toSparkQuantity(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Quantity ) {
                Quantity quantity = (Quantity) obj;
                Map<String,Object> map = new HashMap<>();
                map.put(SYSTEM_TYPE_PROPERTY, "Quantity");
                map.put("unit", quantity.getUnit());
                map.put("value", quantity.getValue());
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkCode(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Code ) {
                Code code = (Code) obj;
                Map<String,Object> map = new HashMap<>();
                map.put(SYSTEM_TYPE_PROPERTY, "Code");
                map.put("code", code.getCode());
                map.put("system", code.getSystem());
                map.put("diasplay", code.getDisplay());
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkInterval(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Interval ) {
                Interval interval = (Interval) obj;
                Map<String,Object> map = new HashMap<>();
                map.put(SYSTEM_TYPE_PROPERTY, "Interval");
                map.put("begin", toSparkType(interval.getLow()));
                map.put("beginInclusive", interval.getLowClosed());
                map.put("end", toSparkType(interval.getHigh()));
                map.put("endInclusive", interval.getHighClosed());
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkRatio(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Ratio ) {
                Ratio ratio = (Ratio) obj;
                Map<String,Object> map = new HashMap<>();
                map.put(SYSTEM_TYPE_PROPERTY, "Ratio");
                map.put("numerator", toSparkType(ratio.getNumerator()));
                map.put("denominator", toSparkType(ratio.getDenominator()));
              
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkTuple(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Tuple ) {
                Tuple tuple = (Tuple) obj;
                
                Map<String,Object> map = tuple.getElements().entrySet().stream()
                        .collect( Collectors.toMap(Map.Entry::getKey, e -> toSparkType(e.getValue())));
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkDataRow(Object obj) {
        Object result = null;
        if( obj != null ) { 
            if( obj instanceof SparkDataRow ) {
                result = ((SparkDataRow) obj).getRow();
            } else {
                result = convertUnhandledValue(obj);
            }
        }
        return result;
    }
    
    public Object toSparkTime(Object obj) {
        Object result = null;
        if( obj != null ) {
            if( obj instanceof Time ) {
                Time time = (Time) obj;
                Map<String,Object> map = new HashMap<>();
                map.put(SYSTEM_TYPE_PROPERTY, "Time");
                map.put("value", toSparkType(time.getTime().toString()));
                
                result = JavaConverters.mapAsScalaMap(map);
            }
        }
        return result;
    }
    
    public Object toSparkType(Object obj) { 
        Object result = null;
        if( obj instanceof Integer ) {
            result = toSparkInteger(obj);
        } else if( obj instanceof BigDecimal ) {
            result = toSparkDecimal(obj);
        } else if( obj instanceof String ) {
            result = toSparkString(obj);
        } else if( obj instanceof Boolean ) {
            result = toSparkBoolean(obj);
        } else if( obj instanceof Date ) { 
            result = toSparkDate(obj);
        } else if( obj instanceof DateTime ) {
            result = toSparkDateTime(obj);
        } else if( obj instanceof List ) {
            result = toSparkList(obj);
        } else if( obj instanceof SparkDataRow ) {
            result = toSparkDataRow(obj);
        } else if( obj instanceof Tuple ) {
            result = toSparkTuple(obj);
        } else if( obj instanceof Quantity ) {
            result = toSparkQuantity(obj);
        } else if( obj instanceof Code ) { 
            result = toSparkCode(obj);
        } else if( obj instanceof Interval ) {
            result = toSparkInterval(obj); 
        } else if( obj instanceof Ratio ) {
            result = toSparkRatio(obj);
        } else if( obj instanceof Time ) {
            result = toSparkTime(obj);
        } else {
            result = convertUnhandledValue(obj);
        }
        return result;
    }

    public Object convertUnhandledValue(Object sparkVal) {
        throw new UnsupportedConversionException(
                String.format("Type %s is not supported", (sparkVal != null) ? sparkVal.getClass().getName() : null));
    }

    protected Date localDateToDate(LocalDate localDate) {
        return new Date(localDate);
    }

    protected DateTime instantToDateTime(Instant instant) {
        return new DateTime(instant.atZone(getZoneOffset()).toOffsetDateTime());
    }

    protected ZoneId getZoneOffset() {
        // TODO : use GMT or pull the value from some configuration (e.g. Context)
        return ZoneOffset.systemDefault();
    }
}
