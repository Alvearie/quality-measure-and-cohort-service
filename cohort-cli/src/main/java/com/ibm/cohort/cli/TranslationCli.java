/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.zip.ZipInputStream;

import org.cqframework.cql.elm.execution.Library;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.engine.ZipStreamLibrarySourceProvider;
import com.ibm.cohort.translator.provider.InJVMCqlTranslationProvider;

public class TranslationCli {

//	public static final LibraryFormat DEFAULT_SOURCE_FORMAT = LibraryFormat.XML;

	//todo trim arguments
	private static final class TranslationOptions extends ConnectionArguments {
		@Parameter(names = { "-f",
				"--files" }, description = "Path to cql file", required = true)
		private String cqlPath;

//		@Parameter(names = { "-s" }, description = "Additional filters to apply to library loaders if supported by the library loading mechansim")
//		private List<String> filters;
//
//		@Parameter(names = { "-l",
//				"--libraryName" }, description = "Library Name", required = true)
//		private String libraryName;
//
//		@Parameter(names = { "-v",
//				"--libraryVersion" }, description = "Library Version", required = false)
//		private String libraryVersion;
//
//		@Parameter(names = { "-e", "--expression" }, description = "ELM Expression(s) to Execute", required = false)
//		private Set<String> expressions;
//
//		@Parameter(names = { "-c",
//				"--context-id" }, description = "Unique ID for one or more context objects (e.g. Patient IDs)", required = true)
//		private List<String> contextIds;
//
//		@Parameter(names = { "-p",
//				"--parameters" }, description = "Parameter value(s) in format name:type:value where value can contain additional parameterized elements separated by comma. Multiple parameters must be specified as multiple -p options", splitter = NoSplittingSplitter.class, required = false)
//		private List<String> parameters;

//		@Parameter(names = { "-s",
//				"--source-format" }, description = "Indicates which files in the file source should be processed", required = false)
//		private LibraryFormat sourceFormat = DEFAULT_SOURCE_FORMAT;

		@Parameter(names = { "-i",
				"--model-info" }, description = "Model info file used when translating CQL")
		private File modelInfoFile;

//		@Parameter(names = {"--logging-level" }, description = "Specific logging level")
//		private LoggingEnum loggingLevel = LoggingEnum.NA;

//		@Parameter(names = { "-h", "--help" }, description = "Display this help", required = false, help = true)
//		private boolean isDisplayHelp;

	}

	public void runWithArgs(TranslationOptions options, PrintStream out) throws Exception {

		//todo assuming that we want to provide a zip file, need a given structure and resolved search paths
		ZipStreamLibrarySourceProvider sourceProvider = new ZipStreamLibrarySourceProvider(new ZipInputStream(new FileInputStream(options.modelInfoFile)), "cql");
		//todo allow non-zip input

		InJVMCqlTranslationProvider provider = new InJVMCqlTranslationProvider();
		provider.addLibrarySourceProvider(sourceProvider);

		Library library = provider.translate(options.cqlPath);

		out.println("Translated Library: ");
		out.println(library.toString());
	}

	public static void main(String[] args) throws Exception {
		TranslationOptions options = new TranslationOptions();
		JCommander jc = JCommander.newBuilder().programName("cql-translation")
				.console(new DefaultConsole(System.out))
				.addObject(options).build();
		jc.parse(args);

		TranslationCli wrapper = new TranslationCli();
		wrapper.runWithArgs(options, System.out);
	}

}
