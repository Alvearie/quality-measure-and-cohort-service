/*
 * (C) Copyright IBM Copr. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.cqfruler;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.FunctionDef;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservationMeasureEvaluation {
	
	private ObservationMeasureEvaluation() {}
	
	private static final Logger logger = LoggerFactory.getLogger(ObservationMeasureEvaluation.class);

	//TODO don't duplicate this method
	private static void clearExpressionCache(Context context) {
        // Hack to clear expression cache
        // See cqf-ruler github issue #153
        try {
            Field privateField = Context.class.getDeclaredField("expressions");
            privateField.setAccessible(true);
            @SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> expressions = (LinkedHashMap<String, Object>) privateField.get(context);
            expressions.clear();

        } catch (Exception e) {
            logger.warn("Error resetting expression cache", e);
        }
    }
	
	public static Resource evaluateObservationCriteria(Context context, Patient patient, Resource resource, Measure.MeasureGroupPopulationComponent pop, MeasureReport report) {
        if (pop == null || !pop.hasCriteria()) {
            return null;
        }

        context.setContextValue("Patient", patient.getIdElement().getIdPart());

        clearExpressionCache(context);

        String observationName = pop.getCriteria().getExpression();
        ExpressionDef ed = context.resolveExpressionRef(observationName);
        if (!(ed instanceof FunctionDef)) {
            throw new IllegalArgumentException(String.format("Measure observation %s does not reference a function definition", observationName));
        }

        Object result = null;
        context.pushWindow();
        try {
            context.push(new Variable().withName(((FunctionDef)ed).getOperand().get(0).getName()).withValue(resource));
            result = ed.getExpression().evaluate(context);
        }
        finally {
            context.popWindow();
        }

        if (result instanceof Resource) {
            return (Resource)result;
        }

        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);
        obs.setId(UUID.randomUUID().toString());
        CodeableConcept cc = new CodeableConcept();
        cc.setText(observationName);
        obs.setCode(cc);
        Extension obsExtension = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/cqf-measureInfo");
        Extension extExtMeasure = new Extension()
                .setUrl("measure")
                .setValue(new CanonicalType("http://hl7.org/fhir/us/cqfmeasures/" + report.getMeasure()));
        obsExtension.addExtension(extExtMeasure);
        Extension extExtPop = new Extension()
                .setUrl("populationId")
                .setValue(new StringType(observationName));
        obsExtension.addExtension(extExtPop);
        obs.addExtension(obsExtension);
        return obs;
    }
}
