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

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cohort.engine.BaseFhirTest;

public class ResourceResolutionProviderTest extends BaseFhirTest {
	public static class DummyProvider extends ResourceResolutionProvider {
	};
	protected ResourceResolutionProvider provider;
	
	@Before
	public void setUp() {
		provider = new DummyProvider();
	}
	
	@Test
	public void when_resolve_by_identifier_no_version_none_matched___null_is_returned() throws Exception {
		
		Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-2.0.0.json", expected);
		
		Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.0.0.json", other);
		
		other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.2.0.json", other);
		
		Measure actual = provider.resolveMeasureByIdentifier(new Identifier().setSystem("my_system").setValue("some_other_identifier"), null);
		assertNull(actual);
	}
	
	@Test
	public void when_resolve_by_identifier_no_version_single_result___result_is_returned() throws Exception {
		
		Measure expected = new Measure();
		expected.addIdentifier().setSystem("my_system").setValue("my_identifier");
		expected.setVersion("1.0.0");
		expected.setUrl("http://alvearie.io/fhir/Measure/MyTestMeasure");
		
		provider.processResource("MyTestMeasure-1.0.0.json", expected);
		
		Measure actual = provider
				.resolveMeasureByIdentifier(new Identifier().setSystem("my_system").setValue("my_identifier"), null);
		
		assertNotNull(actual);
	}
	
	@Test
	public void when_resolve_by_identifier_no_version_multiple_result___latest_version_used() throws Exception {
		
		Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-2.0.0.json", expected);
		
		Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.0.0.json", other);
		
		other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.2.0.json", other);
		
		Measure actual = provider.resolveMeasureByIdentifier(new Identifier().setSystem("my_system").setValue("my_identifier"), null);
		assertNotNull(actual);
		assertEquals( expected.getVersion(), actual.getVersion() );
	}
	
	@Test
	public void when_resolve_by_identifier_with_version_single_match___result_is_returned() throws Exception {
		
		Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-2.0.0.json", expected);
		
		Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.0.0.json", other);
		
		other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.2.0.json", other);
		
		Measure actual = provider.resolveMeasureByIdentifier(new Identifier().setSystem("my_system").setValue("my_identifier"), "2.0.0");
		assertNotNull(actual);
		assertEquals( expected.getVersion(), actual.getVersion() );
	}	
	
	@Test
	public void when_resolve_by_identifier_with_version_multiple_matched___exception_is_thrown() throws Exception {
		
		Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		expected.setId(UUID.randomUUID().toString());
		provider.processResource("MyTestMeasure-2.0.0.json", expected);
		
		Measure other = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		other.setId(UUID.randomUUID().toString());
		provider.processResource("MyTestMeasure-2.0.0.json", other);
		
		other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
		other.setId(UUID.randomUUID().toString());
		provider.processResource("MyTestMeasure-1.2.0.json", other);
		
		assertThrows(IllegalArgumentException.class, () -> provider.resolveMeasureByIdentifier(
				new Identifier().setSystem("my_system").setValue("my_identifier"), "2.0.0"));
	}
	
	@Test
	public void when_resolve_by_identifier_with_version_none_matched___null_is_returned() throws Exception {
		
		Measure expected = getMeasure( "MyTestMeasure", "2.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-2.0.0.json", expected);
		
		Measure other = getMeasure( "MyTestMeasure", "1.0.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.0.0.json", other);
		
		other = getMeasure( "MyTestMeasure", "1.2.0", "my_identifier" );
		provider.processResource("MyTestMeasure-1.2.0.json", other);
		
		Measure actual = provider.resolveMeasureByIdentifier(
				new Identifier().setSystem("my_system").setValue("my_identifier"), "10.0.0");
		assertNull(actual);
	}
	
	protected Measure getMeasure( String name, String version, String identifier ) {
		Measure expected = new Measure();
		expected.setName(name);
		expected.setVersion(version);
		expected.setUrl("http://alvearie.io/fhir/Measure/MyTestMeasure");
		expected.addIdentifier().setSystem("my_system").setValue(identifier);
		return expected;
	}
}
