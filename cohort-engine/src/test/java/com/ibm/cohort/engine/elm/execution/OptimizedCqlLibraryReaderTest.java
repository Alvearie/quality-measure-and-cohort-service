/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.ibm.cohort.engine.elm.execution;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.Library;
import org.junit.Test;

public class OptimizedCqlLibraryReaderTest {
	@Test
	public void testOverrideAndOrEvaluators() throws JAXBException {

		InputStream is = this.getClass().getClassLoader().getResourceAsStream("cql/override/short-circuit-and-or.xml");
		String xml = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining(System.lineSeparator()));
		Library library = OptimizedCqlLibraryReader.read(xml);

		List<ExpressionDef> defs = library.getStatements().getDef();
		Optional<ExpressionDef> orEvaluator = defs.stream().filter(def -> def.getName().equals("TrueOrTrue")).findFirst();
		assertTrue(orEvaluator.isPresent());
		assertThat(orEvaluator.get().getExpression(), instanceOf(ShortOrEvaluator.class));

		Optional<ExpressionDef> andEvaluator = defs.stream().filter(def -> def.getName().equals("TrueAndTrue")).findFirst();
		assertTrue(andEvaluator.isPresent());
		assertThat(andEvaluator.get().getExpression(), instanceOf(ShortAndEvaluator.class));
	}
}