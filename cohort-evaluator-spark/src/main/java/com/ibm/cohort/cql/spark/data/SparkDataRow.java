/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.util.Arrays;
import java.util.Set;
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

    private final SparkTypeConverter typeConverter;
    private final Row sparkRow;

    public SparkDataRow(SparkTypeConverter typeConverter, Row sparkRow) {
        this.typeConverter = typeConverter;
        this.sparkRow = sparkRow;
    }
    
    public Row getRow() {
        return this.sparkRow;
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
            Metadata metadata = MetadataUtils.getColumnMetadata(sparkRow.schema(), fieldName);
            isCode = MetadataUtils.isCodeCol(metadata);
            if (isCode) {
                    Code code = new Code().withCode((String) sparkVal);
                    
                    String system = MetadataUtils.getDefaultSystem(metadata);
                    if( system != null ) {
                        code.withSystem( system );
                    }
 
                    String systemCol = MetadataUtils.getSystemCol(metadata);
                    if (systemCol != null) {
                        code.withSystem(sparkRow.getAs(systemCol));
                    }

                    String displayCol = MetadataUtils.getDisplayCol(metadata);
                    if (displayCol != null) {
                        code.withDisplay(sparkRow.getAs(displayCol));
                    }

                    result = code;
            } else {
                result = doDefaultConversion(sparkVal);
            }
        }

        return result;
    }

    protected Object doDefaultConversion(Object sparkVal) {
        Object result = null;

        try {
            result = typeConverter.toCqlType(sparkVal);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(String.format("Failed to convert field '%s': %s", sparkVal, ex.getMessage()), ex);
        }
        return result;
    }

    @Override
    public Set<String> getFieldNames() {
        return Arrays.stream(sparkRow.schema().fields()).map(StructField::name).collect(Collectors.toSet());
    }
    
    @Override
    public String toString() {
        return sparkRow.toString();
    }
}
