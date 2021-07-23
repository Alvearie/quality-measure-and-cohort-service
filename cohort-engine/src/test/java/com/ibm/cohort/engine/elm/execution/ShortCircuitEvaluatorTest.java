package com.ibm.cohort.engine.elm.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.cqframework.cql.elm.execution.Library;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.opencds.cqf.cql.engine.execution.Context;

import com.ibm.cohort.engine.translation.InJVMCqlTranslationProvider;

public class ShortCircuitEvaluatorTest {

	private Library library;

	@Before
	public void setUp() throws Exception {
		InJVMCqlTranslationProvider provider = new InJVMCqlTranslationProvider();
		library = provider.translate(this.getClass().getClassLoader().getResourceAsStream("cql/override/short-circuit-and-or.cql"));
	}

	@Test
	public void testShortCircuitAnd() {
		Context context = new Context(library);

		verifyAnd(context, "TrueAndTrue", true, false);

		verifyAnd(context, "TrueAndFalse", false, false);

		verifyAnd(context, "TrueAndNull", null, false);

		verifyAnd(context, "FalseAndTrue", false, true);

		verifyAnd(context, "FalseAndFalse", false, true);

		verifyAnd(context, "FalseAndNull", false, true);

		verifyAnd(context, "NullAndTrue", null, false);

		verifyAnd(context, "NullAndFalse", false, false);

		verifyAnd(context, "NullAndNull", null, false);
	}

	@Test
	public void testShortCircuitOr() {
		Context context = new Context(library);

		verifyOr(context, "TrueOrTrue", true, true);

		verifyOr(context, "TrueOrFalse", true, true);

		verifyOr(context, "TrueOrNull", true, true);

		verifyOr(context, "FalseOrTrue", true, false);

		verifyOr(context, "FalseOrFalse", false, false);

		verifyOr(context, "FalseOrNull", null, false);

		verifyOr(context, "NullOrTrue", true, false);

		verifyOr(context, "NullOrFalse", null, false);

		verifyOr(context, "NullOrNull", null, false);
	}

	private void verifyAnd(Context context, String ref, Object expected, boolean shortCircuited) {
		ShortAndEvaluator expression = (ShortAndEvaluator) spy(context.resolveExpressionRef(ref).getExpression());
		Object result = expression.evaluate(context);
		assertThat(result, is(expected));
		verify(expression).getValue(eq(0), any()); // always process left (0-index)
		VerificationMode mode = shortCircuited ? never() : times(1);
		verify(expression, mode).getValue(eq(1), any()); // right (1-index)
	}

	private void verifyOr(Context context, String ref, Object expected, boolean shortCircuited) {
		ShortOrEvaluator expression = (ShortOrEvaluator) spy(context.resolveExpressionRef(ref).getExpression());
		Object result = expression.evaluate(context);
		assertThat(result, is(expected));
		verify(expression).getValue(eq(0), any()); // always process left (0-index)
		VerificationMode mode = shortCircuited ? never() : times(1);
		verify(expression, mode).getValue(eq(1), any()); // right (1-index)
	}
}