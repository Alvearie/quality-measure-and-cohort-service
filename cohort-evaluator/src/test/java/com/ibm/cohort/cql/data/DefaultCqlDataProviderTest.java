/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;

import java.util.ArrayList;

public class DefaultCqlDataProviderTest {

    @Test
    public void getPackageName() {
        String expected = "value";
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.getPackageName())
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.getPackageName());
    }

    @Test
    public void setPackageName() {
        String value = "value";
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        provider.setPackageName(value);
        Mockito.verify(resolver, Mockito.times(1)).setPackageName(value);
    }

    @Test
    public void resolvePath() {
        Object expected = new Object();
        Object target = new Object();
        String path = "path";
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.resolvePath(target, path))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.resolvePath(target, path));
    }

    @Test
    public void getContextPath() {
        Object expected = new Object();
        String contextType = "type";
        String targetType = "path";
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.getContextPath(contextType, targetType))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.getContextPath(contextType, targetType));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void resolveType_stringInput() {
        String typeName = "typeName";
        Class expected = this.getClass();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.resolveType(typeName))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.resolveType(typeName));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void resolveType_objectInput() {
        Object input = new Object();
        Class expected = this.getClass();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.resolveType(input))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.resolveType(input));
    }

    @Test
    public void is() {
        boolean expected = true;
        Object value = new Object();
        Class<?> type = this.getClass();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.is(value, type))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.is(value, type));
    }

    @Test
    public void as() {
        boolean expected = true;
        Object value = new Object();
        Class<?> type = this.getClass();
        boolean isStrict = true;
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.as(value, type, isStrict))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.as(value, type, isStrict));
    }

    @Test
    public void createInstance() {
        Object expected = new Object();
        String typeName = "typeName";
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.createInstance(typeName))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.createInstance(typeName));
    }

    @Test
    public void setValue() {
        Object target = new Object();
        String path = "path";
        Object value = new Object();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        provider.setValue(target, path, value);
        Mockito.verify(resolver, Mockito.times(1))
                .setValue(target, path, value);
    }

    @Test
    public void objectEqual() {
        boolean expected = true;
        Object left = new Object();
        Object right = new Object();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.objectEqual(left, right))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.objectEqual(left, right));
    }

    @Test
    public void objectEquivalent() {
        boolean expected = true;
        Object left = new Object();
        Object right = new Object();
        ModelResolver resolver = Mockito.mock(ModelResolver.class);
        Mockito.when(resolver.objectEquivalent(left, right))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(resolver, null);
        Assert.assertEquals(expected, provider.objectEquivalent(left, right));
    }

    @Test
    public void retrieve() {
        Iterable<Object> expected = new ArrayList<>();
        String context = "context";
        String contextPath = "contextPath";
        Object contextValue = new Object();
        String dataType = "dataType";
        String templateId = "templateId";
        String codePath = "codePath";
        Iterable<Code> codes = new ArrayList<>();
        String valueSet = "valueSet";
        String datePath = "datePath";
        String dateLowPath = "dateLowPath";
        String dateHighPath = "dateHighPath";
        Interval dateRange = Mockito.mock(Interval.class);
        RetrieveProvider retrieveProvider = Mockito.mock(RetrieveProvider.class);
        Mockito.when(retrieveProvider.retrieve(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange))
                .thenReturn(expected);
        CqlDataProvider provider = new DefaultCqlDataProvider(null, retrieveProvider);
        Assert.assertEquals(expected, provider.retrieve(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange));
    }
}
