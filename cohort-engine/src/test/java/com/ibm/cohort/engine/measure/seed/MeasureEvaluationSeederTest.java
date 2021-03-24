package com.ibm.cohort.engine.measure.seed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.ibm.cohort.engine.measure.LibraryHelper;
import org.cqframework.cql.elm.execution.Library.Usings;
import org.cqframework.cql.elm.execution.UsingDef;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

public class MeasureEvaluationSeederTest {

    private final String libraryUrl = "http://measure.evaluation.seeder.test/Library/test-library";
    private final String libraryName = "libraryName";
    private final String libraryVersion = "libraryVersion";

    private final String usingLocalIdentifier = "FHIR";
    private final String usingVersion = "usingVersion";

    private final String fhirUri = "http://hl7.org/fhir";

    private final String periodStart = "2020-01-01";
    private final String periodEnd = "2021-01-01";
    private final String productLine = "productLine";

    private final String defaultParameterKey = "defaultParameterKey";
    private final Type defaultValue = new StringType("defaultValue");
    private final Object resolvedDefault = new Object();

    @Test
    public void create_fullContext() {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        DataProvider dataProvider = Mockito.mock(DataProvider.class);
        Mockito.when(dataProvider.resolvePath(defaultValue, "value"))
                .thenReturn(resolvedDefault);

        Map<String, DataProvider> dataProviders = new HashMap<>();
        dataProviders.put(fhirUri, dataProvider);

        LibraryLoader libraryLoader = Mockito.mock(LibraryLoader.class);
        Mockito.when(libraryLoader.load(createLibraryIdentifier()))
                .thenReturn(createCqlLibrary());

        LibraryResolutionProvider<Library> libraryResourceProvider = Mockito.mock(LibraryResolutionProvider.class);
        Mockito.when(libraryResourceProvider.resolveLibraryByCanonicalUrl(libraryUrl))
                .thenReturn(createLibrary());

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                terminologyProvider,
                dataProviders,
                libraryLoader,
                libraryResourceProvider
        );
        seeder.enableExpressionCaching();

        Measure measure = createMeasure();
        IMeasureEvaluationSeed actual = seeder.create(
                measure, periodStart, periodEnd, productLine
        );

        Interval expectedInterval = createInterval();
        Assert.assertEquals(0, expectedInterval.compareTo(actual.getMeasurementPeriod()));

        Assert.assertSame(measure, actual.getMeasure());
        Assert.assertSame(dataProvider, actual.getDataProvider());

        // Attempt to validate the `Context` by looking for key fields under our control.
        Assert.assertSame(terminologyProvider, actual.getContext().resolveTerminologyProvider());
        Assert.assertSame(resolvedDefault, actual.getContext().resolveParameterRef(null, defaultParameterKey));
        Assert.assertEquals(productLine, actual.getContext().resolveParameterRef(null, "Product Line"));
        Assert.assertTrue(actual.getContext().isExpressionCachingEnabled());
        Assert.assertNotNull(actual.getContext().getDebugMap());
    }

    @Test
    public void create_minimalContext() {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        DataProvider dataProvider = Mockito.mock(DataProvider.class);
        Mockito.when(dataProvider.resolvePath(defaultValue, "value"))
                .thenReturn(resolvedDefault);

        Map<String, DataProvider> dataProviders = new HashMap<>();
        dataProviders.put(fhirUri, dataProvider);

        LibraryLoader libraryLoader = Mockito.mock(LibraryLoader.class);
        Mockito.when(libraryLoader.load(createLibraryIdentifier()))
                .thenReturn(createCqlLibrary());

        LibraryResolutionProvider<Library> libraryResourceProvider = Mockito.mock(LibraryResolutionProvider.class);
        Mockito.when(libraryResourceProvider.resolveLibraryByCanonicalUrl(libraryUrl))
                .thenReturn(createLibrary());

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                terminologyProvider,
                dataProviders,
                libraryLoader,
                libraryResourceProvider
        );
        seeder.disableDebugLogging();

        Measure measure = createMeasure();
        IMeasureEvaluationSeed actual = seeder.create(
                measure, periodStart, periodEnd, null
        );

        Interval expectedInterval = createInterval();
        Assert.assertEquals(0, expectedInterval.compareTo(actual.getMeasurementPeriod()));

        Assert.assertSame(measure, actual.getMeasure());
        Assert.assertSame(dataProvider, actual.getDataProvider());

        // Attempt to validate the `Context` by looking for key fields under our control.
        Assert.assertSame(terminologyProvider, actual.getContext().resolveTerminologyProvider());
        Assert.assertSame(resolvedDefault, actual.getContext().resolveParameterRef(null, defaultParameterKey));
        Assert.assertThrows(NullPointerException.class, () -> actual.getContext().resolveParameterRef(null, "Product Line"));
        Assert.assertFalse(actual.getContext().isExpressionCachingEnabled());
        Assert.assertNull(actual.getContext().getDebugMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_libraryWithMultipleModels() {
        LibraryLoader libraryLoader = Mockito.mock(LibraryLoader.class);
        Mockito.when(libraryLoader.load(createLibraryIdentifier()))
                .thenReturn(createCqlLibraryWithMultipleModels());

        LibraryResolutionProvider<Library> libraryResourceProvider = Mockito.mock(LibraryResolutionProvider.class);
        Mockito.when(libraryResourceProvider.resolveLibraryByCanonicalUrl(libraryUrl))
                .thenReturn(createLibrary());

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                null,
                null,
                libraryLoader,
                libraryResourceProvider
        );

        seeder.create(
                createMeasure(), periodStart, periodEnd, null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_noLibraryOnMeasure() {
        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                null,
                null,
                null,
                null
        );

        seeder.create(
                new Measure(), periodStart, periodEnd, null
        );
    }

    private org.cqframework.cql.elm.execution.Library createCqlLibrary() {
        UsingDef cqlLibraryUsingDef = new UsingDef()
                .withLocalIdentifier(usingLocalIdentifier)
                .withVersion(usingVersion);
        Usings cqlLibraryUsings = new Usings()
                .withDef(cqlLibraryUsingDef);

        return new org.cqframework.cql.elm.execution.Library().withUsings(cqlLibraryUsings);
    }

    private org.cqframework.cql.elm.execution.Library createCqlLibraryWithMultipleModels() {
        UsingDef cqlLibraryUsingDef = new UsingDef()
                .withLocalIdentifier(usingLocalIdentifier)
                .withVersion(usingVersion);
        Usings cqlLibraryUsings = new Usings()
                .withDef(cqlLibraryUsingDef, cqlLibraryUsingDef, cqlLibraryUsingDef);

        return new org.cqframework.cql.elm.execution.Library().withUsings(cqlLibraryUsings);
    }

    private VersionedIdentifier createLibraryIdentifier() {
        return new VersionedIdentifier()
                .withId(libraryName)
                .withVersion(libraryVersion);
    }

    private Library createLibrary() {
        Coding libraryCoding = new Coding();
        libraryCoding.setSystem(LibraryHelper.CODE_SYSTEM_LIBRARY_TYPE);
        libraryCoding.setCode(LibraryHelper.CODE_LOGIC_LIBRARY);

        CodeableConcept libraryType = new CodeableConcept();
        libraryType.setCoding(Collections.singletonList(libraryCoding));

        Library library = new Library();
        library.setType(libraryType);
        library.setName(libraryName);
        library.setVersion(libraryVersion);
        return library;
    }

    private Measure createMeasure() {
        Extension extension = new Extension();
        extension.setUrl(MeasureEvaluationSeeder.PARAMETER_EXTENSION_URL);
        extension.setId(defaultParameterKey);
        extension.setValue(defaultValue);

        CanonicalType libraryRef = new CanonicalType();
        libraryRef.setValue(libraryUrl);

        Measure measure = new Measure();
        measure.setLibrary(Collections.singletonList(libraryRef));
        measure.setExtension(Collections.singletonList(extension));
        return measure;
    }

    private Interval createInterval() {
        LocalDate startDate = LocalDate.parse(periodStart);
        LocalDate endDate = LocalDate.parse(periodEnd);
        Date low = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date high = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return new Interval(low, true, high, true);
    }

}
