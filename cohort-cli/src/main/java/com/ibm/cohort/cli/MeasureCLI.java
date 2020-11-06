/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import static com.ibm.cohort.cli.ParameterHelper.parseParameters;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.MeasureReport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.engine.FhirClientFactory;
import com.ibm.cohort.engine.measure.MeasureEvaluator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureCLI extends BaseCLI {

	/**
	 * Command line argument definitions
	 */
	private static final class Arguments extends ConnectionArguments {

		@Parameter(names = { "-c",
				"--context-id" }, description = "FHIR resource ID for one or more patients to evaluate.", required = true)
		private List<String> contextIds;

		@Parameter(names = { "-p",
				"--parameters" }, description = "Parameter value(s) in format name:type:value where value can contain additional parameterized elements separated by comma", required = false)
		private List<String> parameters;

		@Parameter(names = { "-r",
				"--resource" }, description = "FHIR Resource ID for the measure resorce to be evaluated", required = true )
		private String resourceId;
		
		@Parameter(names = { "-h", "--help" }, description = "Display this help", required = false, help = true)
		private boolean isDisplayHelp;
	}
	
	public MeasureEvaluator runWithArgs(String[] args, PrintStream out) throws Exception {
		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("measure-engine").console(console).addObject(arguments).build();
		jc.parse(args);

		readConnectionConfiguration(arguments);
		
		FhirClientFactory factory = FhirClientFactory.newInstance(FhirContext.forR4());
		IGenericClient dataServerClient = factory.createFhirClient(dataServerConfig);
		IGenericClient terminologyServerClient = factory.createFhirClient(terminologyServerConfig);
		IGenericClient measureServerClient = factory.createFhirClient(measureServerConfig);
		
		Map<String, Object> parameters = null;
		if (arguments.parameters != null) {
			parameters = parseParameters(arguments.parameters);
		}
		
		MeasureEvaluator evaluator = new MeasureEvaluator(dataServerClient, terminologyServerClient, measureServerClient);
		for( String contextId : arguments.contextIds ) {
			out.println("Evaluating: " + contextId);
			MeasureReport report = evaluator.evaluatePatientMeasure(arguments.resourceId, contextId, parameters);
			for( MeasureReport.MeasureReportGroupComponent group : report.getGroup() ) {
				for( MeasureReport.MeasureReportGroupPopulationComponent pop : group.getPopulation() ) {
					String popCode = pop.getCode().getCodingFirstRep().getCode();
					if( pop.getId() != null ) {
						popCode += "(" + pop.getId() + ")";
					}
					out.println( String.format("Population: %s = %d", popCode, pop.getCount() ) );
				}
			}
			out.println("---");
		}
		return evaluator;
	}
	
	public static void main(String[] args) throws Exception {
		MeasureCLI cli = new MeasureCLI();
		cli.runWithArgs( args, System.out );
	}
}
