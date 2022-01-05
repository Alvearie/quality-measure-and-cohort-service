/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;

/**
 * An implementation of {@link CqlDataProvider} that delegates to provided
 * {@link ModelResolver} and {@link RetrieveProvider} instances.
 */
public class DefaultCqlDataProvider implements CqlDataProvider {

    private final ModelResolver modelResolver;
    private final RetrieveProvider retrieveProvider;

    public DefaultCqlDataProvider(ModelResolver modelResolver, RetrieveProvider retrieveProvider) {
        this.modelResolver = modelResolver;
        this.retrieveProvider = retrieveProvider;
    }

    @Override
    public String getPackageName() {
        return modelResolver.getPackageName();
    }

    @Override
    public void setPackageName(String packageName) {
        modelResolver.setPackageName(packageName);
    }

    @Override
    public Object resolvePath(Object target, String path) {
        return modelResolver.resolvePath(target, path);
    }

    @Override
    public Object getContextPath(String contextType, String targetType) {
        return modelResolver.getContextPath(contextType, targetType);
    }

    @Override
    public Class<?> resolveType(String typeName) {
        return modelResolver.resolveType(typeName);
    }

    @Override
    public Class<?> resolveType(Object value) {
        return modelResolver.resolveType(value);
    }

    @Override
    public Boolean is(Object value, Class<?> type) {
        return modelResolver.is(value, type);
    }

    @Override
    public Object as(Object value, Class<?> type, boolean isStrict) {
        return modelResolver.as(value, type, isStrict);
    }

    @Override
    public Object createInstance(String typeName) {
        return modelResolver.createInstance(typeName);
    }

    @Override
    public void setValue(Object target, String path, Object value) {
        modelResolver.setValue(target, path, value);
    }

    @Override
    public Boolean objectEqual(Object left, Object right) {
        return modelResolver.objectEqual(left, right);
    }

    @Override
    public Boolean objectEquivalent(Object left, Object right) {
        return modelResolver.objectEquivalent(left, right);
    }

    @Override
    public Iterable<Object> retrieve(String context, String contextPath, Object contextValue, String dataType, String templateId, String codePath, Iterable<Code> codes, String valueSet, String datePath, String dateLowPath, String dateHighPath, Interval dateRange) {
        return retrieveProvider.retrieve(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange);
    }

}
