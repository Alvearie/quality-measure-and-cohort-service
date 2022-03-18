/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.retrieve;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.cohort.engine.FhirTestBase;
import com.ibm.cohort.engine.terminology.R4RestFhirTerminologyProvider;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class R4RestFhirRetrieveProviderTest extends FhirTestBase {

	R4RestFhirRetrieveProvider provider = null;
	
	@Before
	public void setUp() {
		IGenericClient client = newClient();
		TerminologyProvider termProvider = new R4RestFhirTerminologyProvider(client);
		
		SearchParameterResolver resolver = new SearchParameterResolver(client.getFhirContext());
		
		provider = new R4RestFhirRetrieveProvider(resolver, client);
		provider.setTerminologyProvider(termProvider);
		
		mockFhirResourceRetrieval("/metadata?_format=json", getCapabilityStatement());
	}
	
	@Test
	public void when_no_expand_value_sets_and_modifier_in_search___then_in_modifier_is_included_in_url() {
		
		mockFhirResourceRetrieval("/Condition?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json", new Bundle());
		
		provider.setExpandValueSets(false);
		provider.retrieve("Patient", "subject", "123", "Condition",
				null, "code", null, "MyValueSet",
				null, null, null, null);
		
		verify(getRequestedFor(urlMatching("/Condition\\?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json")));
	}
	
	@Test
	public void when_no_expand_value_sets_with_no_modifier___then_default_implementation_is_used() {
		mockFhirResourceRetrieval("/Condition?subject=Patient%2F123&_format=json", new Bundle());
		
		provider.setExpandValueSets(false);
		provider.retrieve("Patient", "subject", "123", "Condition",
				null, "code", null, null,
				null, null, null, null);
		
		verify(getRequestedFor(urlMatching("/Condition\\?subject=Patient%2F123&_format=json")));
	}
	
	@Test
	public void when_expand_value_sets___then_term_provider_is_used() {
		mockFhirResourceRetrieval("/ValueSet/MyValueSet/$expand?_format=json", makeValueSet("MyValueSet", "http://snomed.info", "123"));
		mockFhirResourceRetrieval("/Condition?code=http%3A%2F%2Fsnomed.info%7C123&subject=Patient%2F123&_format=json", new Bundle());
		
		provider.setExpandValueSets(true);
		provider.retrieve("Patient", "subject", "123", "Condition",
				null, "code", null, "MyValueSet",
				null, null, null, null);
		
		verify(getRequestedFor(urlMatching("/ValueSet/MyValueSet/\\$expand\\?_format=json")));
		verify(getRequestedFor(urlMatching("/Condition\\?code=http%3A%2F%2Fsnomed.info%7C123&subject=Patient%2F123&_format=json")));
	}
	
	@Test
	public void when_search_page_size_is_set___then_count_parameter_is_included_in_url() {
		mockFhirResourceRetrieval("/Condition?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json", new Bundle());
		
		provider.setExpandValueSets(false);
		provider.retrieve("Patient", "subject", "123", "Condition",
				null, "code", null, "MyValueSet",
				null, null, null, null);
		
		verify(getRequestedFor(urlMatching("/Condition\\?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json")));
	}
	
	@Test
	public void when_search_page_size_is_not_set___then_no_count_parameter_is_included_in_url() {
		mockFhirResourceRetrieval("/Condition?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json", new Bundle());
		
		provider.setExpandValueSets(false);
		provider.setSearchPageSize(null);
		provider.retrieve("Patient", "subject", "123", "Condition",
				null, "code", null, "MyValueSet",
				null, null, null, null);
		
		verify(getRequestedFor(urlMatching("/Condition\\?code%3Ain=MyValueSet&subject=Patient%2F123&_format=json")));
	}
	
	protected ValueSet makeValueSet(String name, String system, String... codes) {
		ValueSet vs = new ValueSet();
		vs.setId(name);
		vs.setName(name);
		vs.setUrl("http://somewhere.com/fhir/ValueSet/" + name);
		vs.setVersion("1.0.0");
		for( String code : codes ) {
			vs.getExpansion().addContains().setSystem(system).setCode(code);
		}
		return vs;
	}
}
