package com.ibm.cohort.engine.measure.seed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.elm.execution.Library.Usings;
import org.cqframework.cql.elm.execution.UsingDef;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.common.providers.LibraryResolutionProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.cohort.engine.measure.LibraryHelper;

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

    @Test
    public void create_fullContext() {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        DataProvider dataProvider = Mockito.mock(DataProvider.class);

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
                measure, periodStart, periodEnd, productLine, null
        );

        Interval expectedInterval = createInterval();
        Assert.assertEquals(0, expectedInterval.compareTo(actual.getMeasurementPeriod()));

        Assert.assertSame(measure, actual.getMeasure());
        Assert.assertSame(dataProvider, actual.getDataProvider());

        // Attempt to validate the `Context` by looking for key fields under our control.
        Assert.assertSame(terminologyProvider, actual.getContext().resolveTerminologyProvider());
        Assert.assertEquals(productLine, actual.getContext().resolveParameterRef(null, "Product Line"));
        Assert.assertTrue(actual.getContext().isExpressionCachingEnabled());
        Assert.assertNotNull(actual.getContext().getDebugMap());
    }

    @Test
    public void create_minimalContext() {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        DataProvider dataProvider = Mockito.mock(DataProvider.class);

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
                measure, periodStart, periodEnd, null, null
        );

        Interval expectedInterval = createInterval();
        Assert.assertEquals(0, expectedInterval.compareTo(actual.getMeasurementPeriod()));

        Assert.assertSame(measure, actual.getMeasure());
        Assert.assertSame(dataProvider, actual.getDataProvider());

        // Attempt to validate the `Context` by looking for key fields under our control.
        Assert.assertSame(terminologyProvider, actual.getContext().resolveTerminologyProvider());
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
                createMeasure(), periodStart, periodEnd, null, null
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
                new Measure(), periodStart, periodEnd, null, null
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
        CanonicalType libraryRef = new CanonicalType();
        libraryRef.setValue(libraryUrl);

        Measure measure = new Measure();
        measure.setLibrary(Collections.singletonList(libraryRef));
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
