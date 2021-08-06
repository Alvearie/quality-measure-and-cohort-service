/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.engine;

import java.util.Objects;

import org.opencds.cqf.cql.engine.exception.InvalidCast;
import org.opencds.cqf.cql.engine.model.ModelResolver;

import com.ibm.cohort.datarow.model.DataRow;

/**
 * This is an implementation of the CQL Engine ModelResolver interface for
 * Cohort DataRow objects. Creation and modification of existing DataRow
 * instances is not supported.
 */
public class DataRowModelResolver implements ModelResolver {

    private static final String MODEL_PACKAGE = DataRow.class.getPackage().getName();
    private String packageName = MODEL_PACKAGE;

    public DataRowModelResolver(Class<? extends DataRow> rowImpl) {
        this.packageName = rowImpl.getPackage().getName();
    }
    
    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public Object resolvePath(Object target, String path) {
        DataRow row = (DataRow) target;
        return row.getValue(path);
    }

    @Override
    public Object getContextPath(String contextType, String targetType) {
        // It is assumed that the CQL will only ever use a single context and
        // that the grouping of rows by context path will be done up front
        // during the data preparation phase. No further subdivision of
        // data by context path will be required, so we are nulling out
        // this value.
        return null;
    }

    @Override
    public Class<?> resolveType(String typeName) {
        return DataRow.class;
    }

    @Override
    public Class<?> resolveType(Object value) {
        return value.getClass();
    }

    @Override
    public Boolean is(Object value, Class<?> type) {
        Boolean result = null;
        if (value != null) {
            result = type.isAssignableFrom(value.getClass());
        }
        return result;
    }

    @Override
    public Object as(Object value, Class<?> type, boolean isStrict) {
        Object result = null;
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                result = value;
            } else {
                if (isStrict) {
                    throw new InvalidCast(String.format("Cannot cast a value of type %s as %s.",
                            value.getClass().getName(), type.getName()));
                }
            }
        }
        return result;
    }

    @Override
    public Object createInstance(String typeName) {
        throw new UnsupportedOperationException("Creating rows is not supported");
    }

    @Override
    public void setValue(Object target, String path, Object value) {
        throw new UnsupportedOperationException("Setting values is not supported");
    }

    @Override
    public Boolean objectEqual(Object left, Object right) {
        return Objects.equals(left, right);
    }

    @Override
    public Boolean objectEquivalent(Object left, Object right) {
        return objectEqual(left, right);
    }
}
