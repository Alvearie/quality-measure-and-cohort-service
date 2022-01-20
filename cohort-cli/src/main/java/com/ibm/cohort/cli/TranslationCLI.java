/*
 *
 *  * (C) Copyright IBM Corp. 2021, 2022
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

import org.cqframework.cql.elm.execution.Library;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.DefaultConsole;
import com.ibm.cohort.cql.OptimizedCqlLibraryReader;
import com.ibm.cohort.engine.DirectoryLibrarySourceProvider;
import com.ibm.cohort.translator.provider.CqlTranslationProvider;
import com.ibm.cohort.translator.provider.InJVMCqlTranslationProvider;

public class TranslationCLI {

	private static final class TranslationOptions {
		@Parameter(names = { "-f",
				"--files" }, description = "Path to cql file", required = true)
		private String cqlPath;

		@Parameter(names = { "-b", "--dependency-directory" }, description = "Directory containing additional files necessary for primary file's translation")
		private File directory;

		@Parameter(names = { "-i",
				"--model-info" }, description = "Model info file used when translating CQL")
		private File modelInfoFile;

		@Parameter(names = { "-h", "--help" }, description = "Display this help", required = false, help = true)
		private boolean isDisplayHelp;

	}

	public void runWithArgs(TranslationOptions options, PrintStream out) throws Exception {
		CqlTranslationProvider provider;
		if(options.directory != null && options.directory.exists()){
			DirectoryLibrarySourceProvider librarySourceProvider = new DirectoryLibrarySourceProvider(options.directory.toPath());
			provider = new InJVMCqlTranslationProvider(librarySourceProvider);
		}
		else{
			provider = new InJVMCqlTranslationProvider();
		}

		if (options.modelInfoFile != null && options.modelInfoFile.exists()) {
			provider.convertAndRegisterModelInfo(options.modelInfoFile);
		}

		Library library = OptimizedCqlLibraryReader.read(provider.translate(new FileInputStream(new File(options.cqlPath))));

		out.println("Translated Library: ");
		out.println(library.toString());
	}

	public static void main(String[] args) throws Exception {
		TranslationOptions options = new TranslationOptions();
		JCommander jc = JCommander.newBuilder().programName("cql-translation")
				.console(new DefaultConsole(System.out))
				.addObject(options).build();
		jc.parse(args);

		if (options.isDisplayHelp) {
			jc.usage();
			System.exit(0);
		}

		TranslationCLI wrapper = new TranslationCLI();
		wrapper.runWithArgs(options, System.out);
	}

}
