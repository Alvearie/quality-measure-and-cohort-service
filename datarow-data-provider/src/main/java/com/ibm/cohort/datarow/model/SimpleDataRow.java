/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Map-based implementation of DataRow. Contents of the map are expected
 * to be in whatever final form is required for the CQL Engine. For example,
 * date and datetime values should be in the CQL Runtime typesystem. 
 */
public class SimpleDataRow implements Serializable, DataRow {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, Object> fields;

    public SimpleDataRow(Map<String, Object> fields) {
        this.fields = fields;
    }

    @Override
    public Object getValue(String fieldInfo) {
        return fields.get(fieldInfo);
    }

    @Override
    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    @Override
    public String toString() {
        return "DataRow{" +
                "fields=" + fields +
                '}';
    }
    
    @Override 
    public boolean equals(Object rhs) {
    	boolean isEqual = false;
    	if( rhs != null && rhs instanceof DataRow ) {
    		DataRow row = (DataRow) rhs;
    		if( getFieldNames().equals(row.getFieldNames()) ) {
	    		for( String fieldName : getFieldNames() ) { 
		    		isEqual = Objects.equals(getValue(fieldName), row.getValue(fieldName));
		    		if( ! isEqual ) { 
		    			break;
		    		}
		    	}
    		}
    	}
    	return isEqual;
    }
    
    @Override
    public int hashCode() {
    	return fields.hashCode();
    }
}
