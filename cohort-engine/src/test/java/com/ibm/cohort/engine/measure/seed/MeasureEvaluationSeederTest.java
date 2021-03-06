package com.ibm.cohort.engine.measure.seed;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.elm.execution.Library;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.DefaultLibraryLoader;

@RunWith(MockitoJUnitRunner.class)
public class MeasureEvaluationSeederTest {

    private MeasureEvaluationSeeder seeder;
    private Library library;
    private Context context;
    private Map<String, DataProvider> dataProviders;
    private String modelUri = "fakeUri";

    @Mock
    private DataProvider dataProvider;

    @Before
    public void setup() {
       seeder = Mockito.spy(new MeasureEvaluationSeeder(null, dataProviders, new DefaultLibraryLoader(), null));
       library = new Library();
       context = Mockito.spy(new Context(library));
       doReturn(context).when(seeder).createDefaultContext(any());

       dataProviders = new HashMap<>();
       dataProviders.put(modelUri, dataProvider);
    }

    @Test
    public void createContextDisabled() {
        seeder.disableDebugLogging();

        Context context = seeder.createContext(
                library,
                modelUri,
                dataProviders.get(modelUri),
                seeder.createMeasurePeriod("2020-01-01", "2021-01-01"),
                "productLine");

        Assert.assertFalse(context.isExpressionCachingEnabled());
        Assert.assertNull(context.getDebugMap());
    }

    @Test
    public void createContextFullyEnabled() {
        seeder.enableExpressionCaching();

        String productLine = "productLine";
        Context context = seeder.createContext(
                library,
                modelUri,
                dataProviders.get(modelUri),
                seeder.createMeasurePeriod("2020-01-01", "2021-01-01"),
                productLine);

        verify(context).registerTerminologyProvider(any());
        verify(context).registerDataProvider(modelUri, dataProvider);
        verify(context).setParameter(null, "Product Line", productLine);
        Assert.assertTrue(context.isExpressionCachingEnabled());
        Assert.assertTrue(context.getDebugMap().getIsLoggingEnabled());
    }
}
