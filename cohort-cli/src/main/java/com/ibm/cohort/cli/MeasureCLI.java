/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.hl7.fhir.r4.model.MeasureReport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.cli.input.MeasureContextProvider;
import com.ibm.cohort.cli.input.NoSplittingSplitter;
import com.ibm.cohort.engine.measure.MeasureContext;
import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureCLI extends BaseCLI {

	private static enum ReportFormat { TEXT, JSON }
	
	/**
	 * Command line argument definitions
	 */
	private static final class Arguments extends ConnectionArguments {

		@Parameter(names = { "-c",
				"--context-id" }, description = "FHIR resource ID for one or more patients to evaluate.", required = true)
		private List<String> contextIds;
		
		@Parameter(names = { "-h", "--help" }, description = "Display this help", required = false, help = true)
		private boolean isDisplayHelp;
		
		@Parameter(names = { "-f", "--format" }, description = "Output format of the report (JSON|TEXT*)" ) 
		private ReportFormat reportFormat = ReportFormat.TEXT;

		@Parameter(names = { "-j",
				"--json-measure-configurations" }, description = "JSON File containing measure resource ids and optional parameters. Cannot be specified if -r option is used")
		private File measureConfigurationFile;

		@Parameter(names = { "-p",
				"--parameters" }, description = "Parameter value(s) in format name:type:value where value can contain additional parameterized elements separated by comma. Multiple parameters must be specified as multiple -p options", splitter = NoSplittingSplitter.class, required = false)
		private List<String> parameters;

		@Parameter(names = { "-r",
				"--resource" }, description = "FHIR Resource ID for the measure resource to be evaluated. Cannot be specified if -j option is used")
		private String resourceId;

		public void validate() {
			boolean resourceSpecified = resourceId != null;
			boolean measureConfigurationSpecified = measureConfigurationFile != null;

			if (resourceSpecified ==  measureConfigurationSpecified) {
				throw new IllegalArgumentException("Must specify exactly one of -r or -j options");
			}

			if (measureConfigurationSpecified && !measureConfigurationFile.exists()) {
				throw new IllegalArgumentException("Measure configuration file does not exist: " + measureConfigurationFile.getPath());
			}
		}
	}
	
	public MeasureEvaluator runWithArgs(String[] args, PrintStream out) throws Exception {
		MeasureEvaluator evaluator = null;

		Arguments arguments = new Arguments();
		Console console = new DefaultConsole(out);
		JCommander jc = JCommander.newBuilder().programName("measure-engine").console(console).addObject(arguments).build();
		jc.parse(args);

		if( arguments.isDisplayHelp ) {
			jc.usage();
		} else {
			arguments.validate();

			readConnectionConfiguration(arguments);
			
			FhirContext fhirContext = FhirContext.forR4();
			
			FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
			FhirClientBuilder builder = factory.newFhirClientBuilder(fhirContext);
			
			IGenericClient dataServerClient = builder.createFhirClient(dataServerConfig);
			IGenericClient terminologyServerClient = builder.createFhirClient(terminologyServerConfig);
			IGenericClient measureServerClient = builder.createFhirClient(measureServerConfig);
			
			List<MeasureContext> measureContexts;

			if (arguments.measureConfigurationFile != null) {
				measureContexts = MeasureContextProvider.getMeasureContexts(arguments.measureConfigurationFile);
			} else {
				measureContexts = MeasureContextProvider.getMeasureContexts(arguments.resourceId,  arguments.parameters);
			}
			
			evaluator = new MeasureEvaluator(dataServerClient, terminologyServerClient, measureServerClient);
			for( String contextId : arguments.contextIds ) {
				out.println("Evaluating: " + contextId);
				// Reports only returned for measures where patient is in initial population
				List<MeasureReport> reports = evaluator.evaluatePatientMeasures(contextId, measureContexts);

				for (MeasureReport report : reports) {
					if (arguments.reportFormat == ReportFormat.TEXT) {
						out.println("Result for " + report.getMeasure());
						for (MeasureReport.MeasureReportGroupComponent group : report.getGroup()) {
							for (MeasureReport.MeasureReportGroupPopulationComponent pop : group.getPopulation()) {
								String popCode = pop.getCode().getCodingFirstRep().getCode();
								if (pop.getId() != null) {
									popCode += "(" + pop.getId() + ")";
								}
								out.println(String.format("Population: %s = %d", popCode, pop.getCount()));
							}
						}
					} else {
						IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
						out.println(parser.encodeResourceToString(report));
					}
					out.println("---");
				}
				if (reports.isEmpty()) {
					out.println("---");
				}
			}
		}
		return evaluator;
	}
	
	public static void main(String[] args) throws Exception {
		MeasureCLI cli = new MeasureCLI();
		cli.runWithArgs( args, System.out );
	}
}
