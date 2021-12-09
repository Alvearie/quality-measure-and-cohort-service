/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ContextDefinitionsTest {

	@Test
	public void testGetContextDefinitionByNameNoDefinitionsReturnsNull() {
		ContextDefinition contextDefinition1 = new ContextDefinition();
		contextDefinition1.setName("context1");
		
		ContextDefinition contextDefinition2 = new ContextDefinition();
		contextDefinition2.setName("context2");
		
		List<ContextDefinition> contextDefinitionList = Arrays.asList(contextDefinition1, contextDefinition2);
		
		ContextDefinitions contextDefinitions = new ContextDefinitions();
		contextDefinitions.setContextDefinitions(contextDefinitionList);
		
		assertNull(contextDefinitions.getContextDefinitionByName("context3"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetContextDefinitionByNameDuplicateContextThrowsException() {
		ContextDefinition contextDefinition1 = new ContextDefinition();
		contextDefinition1.setName("context1");

		ContextDefinition contextDefinition2 = new ContextDefinition();
		contextDefinition2.setName("context1");

		List<ContextDefinition> contextDefinitionList = Arrays.asList(contextDefinition1, contextDefinition2);

		ContextDefinitions contextDefinitions = new ContextDefinitions();
		contextDefinitions.setContextDefinitions(contextDefinitionList);

		contextDefinitions.getContextDefinitionByName("context1");
	}

	@Test
	public void testGetContextDefinitionByNameExactlyOneDefinitionReturned() {
		ContextDefinition contextDefinition1 = new ContextDefinition();
		contextDefinition1.setName("context1");

		ContextDefinition contextDefinition2 = new ContextDefinition();
		contextDefinition2.setName("context2");

		List<ContextDefinition> contextDefinitionList = Arrays.asList(contextDefinition1, contextDefinition2);

		ContextDefinitions contextDefinitions = new ContextDefinitions();
		contextDefinitions.setContextDefinitions(contextDefinitionList);

		assertEquals(contextDefinition1, contextDefinitions.getContextDefinitionByName("context1"));
	}
}