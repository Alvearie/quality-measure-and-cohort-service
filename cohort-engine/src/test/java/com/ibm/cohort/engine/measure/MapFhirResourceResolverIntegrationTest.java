/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.UUID;

import com.ibm.cohort.cql.fhir.handler.ResourceFieldHandler;
import com.ibm.cohort.cql.fhir.resolver.MapFhirResourceResolver;
import com.ibm.cohort.cql.hapi.handler.R4MeasureResourceFieldHandler;
import com.ibm.cohort.cql.version.ResourceSelector;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.engine.BaseFhirTest;

public class MapFhirResourceResolverIntegrationTest extends BaseFhirTest {

    private MapFhirResourceResolver<Measure, Identifier> resolver;

    @Before
    public void setUp() {
        ResourceFieldHandler<Measure, Identifier> measureFieldHandler = new R4MeasureResourceFieldHandler();
        ResourceSelector<Measure> resourceSelector = new ResourceSelector<>(measureFieldHandler);
        resolver = new MapFhirResourceResolver<>(measureFieldHandler, resourceSelector);
    }

    @Test
    public void when_resolve_by_identifier_no_version_none_matched___null_is_returned() {
        Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByIdentifier("some_other_identifier", "my_system", null);
        assertNull(actual);
    }

    @Test
    public void when_resolve_by_identifier_no_version_single_result___result_is_returned() {
        Measure expected = new Measure();
        expected.setId("test-id");
        expected.addIdentifier().setSystem("my_system").setValue("my_identifier");
        expected.setVersion("1.0.0");
        expected.setUrl("http://alvearie.io/fhir/Measure/MyTestMeasure");

        resolver.addResource(expected);

        Measure actual = resolver.resolveByIdentifier("my_identifier", "my_system", null);
        assertNotNull(actual);
    }

    @Test
    public void when_resolve_by_identifier_no_version_multiple_result___latest_version_used() {
        Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByIdentifier("my_identifier", "my_system", null);
        assertNotNull(actual);
        assertEquals( expected.getVersion(), actual.getVersion() );
    }

    @Test
    public void when_resolve_by_identifier_with_version_single_match___result_is_returned() {
        Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByIdentifier("my_identifier", "my_system", "2.0.0");
        assertNotNull(actual);
        assertEquals( expected.getVersion(), actual.getVersion() );
    }

    @Test
    public void when_resolve_by_identifier_with_version_multiple_matched___exception_is_thrown() {
        Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        expected.setId(UUID.randomUUID().toString());
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        other.setId(UUID.randomUUID().toString());
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        other.setId(UUID.randomUUID().toString());
        resolver.addResource(other);

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolveByIdentifier("my_identifier", "my_system", "2.0.0")
        );
    }

    @Test
    public void when_resolve_by_identifier_with_version_none_matched___null_is_returned() {
        Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByIdentifier("my_identifier", "my_system", "10.0.0");
        assertNull(actual);
    }

    @Test
    public void when_resolve_by_name_with_version_none_matched___null_is_returned() {
        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByName("Other", null);
        assertNull(actual);
    }

    @Test
    public void when_resolve_by_name_with_version_multiple_result___latest_semantic_version_returned() {
        Measure expected = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "9.5.zzzzz", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByName("MyTestMeasure", null);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void when_resolve_by_name_with_version_single_result___return_single_result() {
        Measure expected = getMeasure( "MyTestMeasure", "9.5.zzzzz", "my_identifier" );
        resolver.addResource(expected);

        Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
        resolver.addResource(other);

        other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
        resolver.addResource(other);

        Measure actual = resolver.resolveByName("MyTestMeasure", "9.5.zzzzz");
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    protected Measure getMeasure( String name, String version, String identifier ) {
        Measure expected = new Measure();
        expected.setId(name + "-id");
        expected.setName(name);
        expected.setVersion(version);
        expected.setUrl("http://alvearie.io/fhir/Measure/MyTestMeasure");
        expected.addIdentifier().setSystem("my_system").setValue(identifier);
        return expected;
    }

}
