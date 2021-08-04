/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.opencds.cqf.cql.engine.runtime.Code;

import com.ibm.cohort.datarow.model.DataRow;

/**
 * Implements the DataRow interface using the data provided by an Apache Spark
 * Row object. DataType conversion is supplied for all the Spark SQL data types
 * except those of structured data - map, list, row - and binary data that
 * doesn't have an equivalent for CQL.
 * 
 * The CQL language has a rich set of functionality around dealing with coded
 * data, but there is no corresponding type in the Spark SQL datatypes. In order
 * to support conversion of a column in the Spark Row to a CQL System.Code
 * object, the conversion logic supports Spark column metadata. If a column
 * contains the boolean value true under the key "isCodeCol", then the column is
 * assumed to be a String value and will be automatically converted to a CQL
 * code where System.Code.code is set to the column value. Optionally, the
 * metadata may contain systemCol and displayCol metadata that indicate other
 * columns in the row that contain the System.Code.system and
 * System.Code.display field data that corresponds with the tagged column.
 * 
 * Conversion of other CQL-native types, such as interval and ratio, are left to
 * the CQL author to handle.
 */
public class SparkDataRow implements DataRow {

    public static final String IS_CODE_COL = "isCodeCol";
    public static final String SYSTEM_COL = "systemCol";
    public static final String DISPLAY_COL = "dispalyCol";

    protected static final Map<Class<?>, Function<Object, Object>> CONVERTERS = new HashMap<>();
    static {
        CONVERTERS.put(Boolean.class, SparkTypeConverter::toCqlBoolean);
        CONVERTERS.put(Integer.class, SparkTypeConverter::toCqlInteger);
        CONVERTERS.put(String.class, SparkTypeConverter::toCqlString);
        CONVERTERS.put(BigDecimal.class, SparkTypeConverter::toCqlDecimal);
        CONVERTERS.put(Byte.class, SparkTypeConverter::toCqlByte);
        CONVERTERS.put(Short.class, SparkTypeConverter::toCqlShort);
        CONVERTERS.put(Long.class, SparkTypeConverter::toCqlLong);
        CONVERTERS.put(Float.class, SparkTypeConverter::toCqlFloat);
        CONVERTERS.put(Double.class, SparkTypeConverter::toCqlDouble);
        CONVERTERS.put(LocalDate.class, SparkTypeConverter::toCqlDate);
        CONVERTERS.put(Instant.class, SparkTypeConverter::toCqlDateTime);
        CONVERTERS.put(java.sql.Date.class, SparkTypeConverter::toCqlDate);
        CONVERTERS.put(java.sql.Timestamp.class, SparkTypeConverter::toCqlDateTime);
        CONVERTERS.put(scala.collection.Seq.class, SparkTypeConverter::toCqlList);
        CONVERTERS.put(scala.collection.Map.class, SparkTypeConverter::toCqlTuple);
        CONVERTERS.put(Row.class, SparkTypeConverter::toCqlTuple);
        CONVERTERS.put(byte[].class, SparkTypeConverter::toCqlBinary);
    }

    private final Row sparkRow;

    public SparkDataRow(Row sparkRow) {
        this.sparkRow = sparkRow;
    }

    @Override
    public Object getValue(String fieldName) {
        Object result = null;

        Object sparkVal = sparkRow.getAs(fieldName);

        if (sparkVal != null) {
            boolean isCode = false;

            /**
             * The following logic uses column metadata fields provided in the Spark schema
             * to identify columns
             */
            Metadata metadata = sparkRow.schema().fields()[sparkRow.schema().fieldIndex(fieldName)].metadata();
            if (metadata.contains(IS_CODE_COL)) {
                isCode = metadata.getBoolean(IS_CODE_COL);
                if (isCode) {
                    Code code = new Code().withCode((String) sparkVal);

                    if (metadata.contains(SYSTEM_COL)) {
                        String systemField = metadata.getString(SYSTEM_COL);
                        if (systemField != null) {
                            code.withSystem(sparkRow.getAs(systemField));
                        }
                    }

                    if (metadata.contains(DISPLAY_COL)) {
                        String displayField = metadata.getString(DISPLAY_COL);
                        if (displayField != null) {
                            code.withDisplay(sparkRow.getAs(displayField));
                        }
                    }

                    result = code;
                } else {
                    result = doDefaultConversion(sparkVal);
                }
            } else {
                result = doDefaultConversion(sparkVal);
            }
        }

        return result;
    }

    protected Object doDefaultConversion(Object sparkVal) {
        Object result = null;

        Function<Object, Object> converter = CONVERTERS.get(sparkVal.getClass());
        try {
            if (converter != null) {
                result = converter.apply(sparkVal);
            } else {
                SparkTypeConverter.convertUnhandledValue(sparkVal);
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(String.format("Failed to convert field '%s': %s", ex.getMessage()), ex);
        }
        return result;
    }

    @Override
    public Set<String> getFieldNames() {
        return Arrays.stream(sparkRow.schema().fields()).map(StructField::name).collect(Collectors.toSet());
    }
}
