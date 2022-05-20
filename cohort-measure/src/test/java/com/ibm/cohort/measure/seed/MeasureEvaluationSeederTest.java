/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.measure.seed;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.fhir.resolver.FhirResourceResolver;
import com.ibm.cohort.cql.hapi.R4LibraryDependencyGatherer;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.codesystems.LibraryType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

public class MeasureEvaluationSeederTest {

    private final String libraryUrl = "http://measure.evaluation.seeder.test/Library/test-library";
    private final String libraryName = "Test";
    private final String libraryVersion = "1.0.0";

    private final String fhirUri = "http://hl7.org/fhir";

    private final String periodStart = "2020-01-01";
    private final String periodEnd = "2021-01-01";
    private final String productLine = "productLine";

    @Test
    public void create_fullContext() throws IOException {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        CqlDataProvider dataProvider = Mockito.mock(CqlDataProvider.class);

        Map<String, CqlDataProvider> dataProviders = new HashMap<>();
        dataProviders.put(fhirUri, dataProvider);
        Measure measure = createMeasure();

        Library library = createLibrary("/cql/seeder/Test-1.0.0.xml");
        FhirResourceResolver<Library> libraryResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(libraryResolver.resolveByName(libraryName, libraryVersion))
                .thenReturn(library);

        R4LibraryDependencyGatherer dependencyGatherer = Mockito.mock(R4LibraryDependencyGatherer.class);
        Mockito.when(dependencyGatherer.gatherForMeasure(measure))
                .thenReturn(Collections.singletonList(library));

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                terminologyProvider,
                dataProviders,
                dependencyGatherer,
                libraryResolver
        );
        seeder.enableExpressionCaching();

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
    public void create_minimalContext() throws IOException {
        TerminologyProvider terminologyProvider = Mockito.mock(TerminologyProvider.class);

        CqlDataProvider dataProvider = Mockito.mock(CqlDataProvider.class);

        Map<String, CqlDataProvider> dataProviders = new HashMap<>();
        dataProviders.put(fhirUri, dataProvider);

        Library library = createLibrary("/cql/seeder/Test-1.0.0.xml");
        FhirResourceResolver<Library> libraryResolver = Mockito.mock(FhirResourceResolver.class);
        Mockito.when(libraryResolver.resolveByName(libraryName, libraryVersion))
                .thenReturn(library);

        Measure measure = createMeasure();

        R4LibraryDependencyGatherer dependencyGatherer = Mockito.mock(R4LibraryDependencyGatherer.class);
        Mockito.when(dependencyGatherer.gatherForMeasure(measure))
                .thenReturn(Collections.singletonList(library));

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                terminologyProvider,
                dataProviders,
                dependencyGatherer,
                libraryResolver
        );
        seeder.disableDebugLogging();

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
    public void create_libraryWithMultipleModels() throws IOException {
        Measure measure = createMeasure();

        FhirResourceResolver<Library> libraryResolver = Mockito.mock(FhirResourceResolver.class);

        R4LibraryDependencyGatherer dependencyGatherer = Mockito.mock(R4LibraryDependencyGatherer.class);
        Mockito.when(dependencyGatherer.gatherForMeasure(measure))
                .thenReturn(Collections.singletonList(createLibrary("/cql/seeder/MultipleUsings-1.0.0.xml")));

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                null,
                null,
                dependencyGatherer,
                libraryResolver
        );

        seeder.create(
                measure, periodStart, periodEnd, null, null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_noLibraryOnMeasure() {
        Measure measure = createMeasure();

        FhirResourceResolver<Library> libraryResolver = Mockito.mock(FhirResourceResolver.class);

        R4LibraryDependencyGatherer dependencyGatherer = Mockito.mock(R4LibraryDependencyGatherer.class);
        Mockito.when(dependencyGatherer.gatherForMeasure(measure))
                .thenReturn(Collections.emptyList());

        MeasureEvaluationSeeder seeder = new MeasureEvaluationSeeder(
                null,
                null,
                dependencyGatherer,
                libraryResolver
        );

        seeder.create(
                measure, periodStart, periodEnd, null, null
        );
    }

    private Library createLibrary(String elmFile) throws IOException {
        Coding libraryCoding = new Coding();
        libraryCoding.setSystem(LibraryType.LOGICLIBRARY.getSystem());
        libraryCoding.setCode(LibraryType.LOGICLIBRARY.toCode());

        CodeableConcept libraryType = new CodeableConcept();
        libraryType.setCoding(Collections.singletonList(libraryCoding));

        Library library = new Library();
        library.setId(libraryName + "-id");
        library.setType(libraryType);
        library.setName(libraryName);
        library.setVersion(libraryVersion);

        Attachment attachment = new Attachment();
        attachment.setData(IOUtils.resourceToByteArray(elmFile));
        attachment.setContentType("application/elm+xml");
        library.setContent(Collections.singletonList(attachment));
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
        return new Interval(new DateTime(periodStart + "T00:00:00.0", ZoneOffset.UTC), true,
                            new DateTime(periodEnd + "T23:59:59.999", ZoneOffset.UTC), true);
    }

}
