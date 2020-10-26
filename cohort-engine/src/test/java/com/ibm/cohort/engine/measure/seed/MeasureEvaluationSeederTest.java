package com.ibm.cohort.engine.measure.seed;

import java.util.HashMap;

import org.cqframework.cql.elm.execution.Library;
import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.DefaultLibraryLoader;

public class MeasureEvaluationSeederTest {

    @Test
    public void createContext() {
        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(null, new DefaultLibraryLoader(), null);

        seeder.disableDebugLogging();
        seeder.disableExpressionCaching();
        seeder.withTerminologyProvider("source", "user", "password");

        Context context = seeder.createContext(
                new Library(),
                new HashMap<>(),
                null,
                seeder.createMeasurePeriod("2020-01-01", "2021-01-01"),
                "productLine");

        Assert.assertFalse(context.isExpressionCachingEnabled());
        Assert.assertNull(context.getDebugMap());
    }
}
