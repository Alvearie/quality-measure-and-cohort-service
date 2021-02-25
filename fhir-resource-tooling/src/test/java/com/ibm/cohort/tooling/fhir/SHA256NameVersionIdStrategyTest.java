/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.tooling.fhir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;
import org.junit.Before;
import org.junit.Test;

public class SHA256NameVersionIdStrategyTest {
	IdStrategy strategy;
	
	@Before
	public void setUp() {
		strategy = new SHA256NameVersionIdStrategy();
	}
	
	@Test
	public void max_name_version_length___output_meets_fhir_constraints() throws Exception { 
		String name = RandomStringUtils.random(256, /*useLetters=*/true, /*useNumbers=*/true);
		
		// There is no defined upper limit here, but we are guessing
		// that three digits for each dotted triplet entry + 
		// delimiter + some label stuff isn't much bigger than this
		String version = RandomStringUtils.random(20, /*useLetters=*/true, /*useNumbers=*/true);;
		
		MetadataResource resource = new Measure();
		resource.setName(name);
		resource.setVersion(version);
		
		String generated = strategy.generateId(resource);
		assertTrue( generated.matches("[A-Za-z0-9\\-\\.]{1,64}") );
	}
	
	@Test
	public void different_versions_same_name___output_different_ids() throws Exception { 
		String name = "COL-InitialPopulation";
		
		List<String> versions = Arrays.asList("1.0.0", "1.2.0", "2.1.0");
		
		Set<String> ids = new HashSet<>();
		for( String version : versions ) {
			MetadataResource resource = new Measure();
			resource.setName(name);
			resource.setVersion(version);
			
			ids.add( strategy.generateId(resource) );			
		}
		assertEquals( versions.size(), ids.size() );
	}
	
	@Test
	public void same_input_multiple_times___always_same_output() throws Exception { 
		String name = "COL-Denominator";
		String version = "1.1.0";
		
		MetadataResource resource = new Measure();
		resource.setName(name);
		resource.setVersion(version);
		
		Set<String> ids = new HashSet<>();
		for( int i=0; i<1000; i++ ) {
			ids.add( strategy.generateId(resource) );			
		}
		assertEquals( 1, ids.size() );
	}
}
