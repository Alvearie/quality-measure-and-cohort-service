package com.ibm.cohort.engine.cqfruler;

import static org.hl7.fhir.r4.model.MeasureReport.MeasureReportType.SUBJECTLIST;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opencds.cqf.common.evaluation.MeasureScoring.PROPORTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

public class MeasureEvaluationTest {
	@Test
	public void testCacheCleared() {
		DataProvider provider = mock(DataProvider.class);
		Interval period = mock(Interval.class);

		MeasureEvaluation evaluation = spy(new MeasureEvaluation(provider, period));
		doReturn(true).when(evaluation).evaluatePopulationCriteria(any(), any(), any(), any(), any(), any(), any(), any());

		List<Patient> patients = new ArrayList<>();
		patients.add(new Patient());
		patients.add(new Patient());

		Measure measure = new Measure();
		measure.setScoring(new CodeableConcept().addCoding(new Coding().setCode(PROPORTION.toCode())));
		Measure.MeasureGroupComponent group = new Measure.MeasureGroupComponent();
		measure.setGroup(Collections.singletonList(group));

		Context context = new Context(new Library());
		evaluation.evaluate(measure, context, patients, SUBJECTLIST, false, false);

		verify(evaluation, times(2)).clearExpressionCache(any());
	}
}