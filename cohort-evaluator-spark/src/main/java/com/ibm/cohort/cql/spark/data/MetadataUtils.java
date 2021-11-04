/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructType;

public class MetadataUtils {
    
    public static final String IS_CODE_COL = "isCodeCol";
    public static final String SYSTEM_COL = "systemCol";
    public static final String DISPLAY_COL = "displayCol";
    public static final String SYSTEM = "system";
    
    public static Metadata getColumnMetadata(StructType schema, String colName) {
        return schema.fields()[ schema.fieldIndex(colName) ].metadata();
    }
    
    public static boolean isCodeCol(Metadata columnMetadata) {
        return columnMetadata.contains(IS_CODE_COL) && columnMetadata.getBoolean(IS_CODE_COL);
    }
    
    public static boolean hasSystemCol(Metadata columnMetadata) {
        return columnMetadata.contains(SYSTEM_COL);
    }
    
    public static String getSystemCol(Metadata columnMetadata) {
        return hasSystemCol(columnMetadata) ? columnMetadata.getString(SYSTEM_COL) : null;
    }
    
    public static boolean hasDisplayCol(Metadata columnMetadata) {
        return columnMetadata.contains(DISPLAY_COL);
    }
    
    public static String getDisplayCol(Metadata columnMetadata) {
        return hasDisplayCol(columnMetadata) ? columnMetadata.getString(DISPLAY_COL) : null;
    }
    
    public static String getDefaultSystem(Metadata columnMetadata) {
        return columnMetadata.contains(SYSTEM) ? columnMetadata.getString(SYSTEM) : null;
    }
}
