/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Base64;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FhirLibraryLibrarySourceProviderTest {

	FhirLibraryLibrarySourceProvider provider = null;
	
	@Before
	public void setUp() {
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080");
		provider = new FhirLibraryLibrarySourceProvider(client);
	}
	
	@Test
	public void testLibraryWithCQLNoIncludes__loadSuccess() {
		String cql = "library \"Test\" version '1.0.0'\n" + 
				"using \"FHIR\" version '4.0.0'\n" +
				"context Patient\n" + 
				"define DoSomething: 'hello, world'";
		
		Attachment attachment = new Attachment();
		attachment.setContentType("text/cql");
		attachment.setData( Base64.getEncoder().encode( cql.getBytes() ) );
		
		Library library = new Library();
		library.setContent(Arrays.asList(attachment));
		
		int loaded = provider.loadRecursively(library);
		assertEquals( 1, loaded );
	}
}
