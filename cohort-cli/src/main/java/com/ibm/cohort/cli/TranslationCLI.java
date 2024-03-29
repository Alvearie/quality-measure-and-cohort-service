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
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.ibm.cohort.cql.library.ClasspathCqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.Format;
import com.ibm.cohort.cql.library.MapCqlLibraryProvider;
import com.ibm.cohort.cql.library.MapCqlLibraryProviderFactory;
import com.ibm.cohort.cql.library.PriorityCqlLibraryProvider;
import com.ibm.cohort.cql.translation.CqlToElmTranslator;
import com.ibm.cohort.cql.translation.CqlTranslationResult;
import com.ibm.cohort.cql.provider.ProviderBasedCqlLibrarySourceProvider;
import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.DefaultConsole;

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
		CqlLibraryProvider libraryProvider;
		if(options.directory != null && options.directory.exists()){
			MapCqlLibraryProviderFactory libraryProviderFactory = new MapCqlLibraryProviderFactory();
			libraryProvider = libraryProviderFactory.fromDirectory(options.directory.toPath());
		}
		else{
			libraryProvider = new MapCqlLibraryProvider(Collections.emptyMap());
		}

		CqlLibraryProvider fhirClasspathProvider = new ClasspathCqlLibraryProvider();
		libraryProvider = new PriorityCqlLibraryProvider(libraryProvider, fhirClasspathProvider);

		CqlToElmTranslator translator = new CqlToElmTranslator();
		if (options.modelInfoFile != null && options.modelInfoFile.exists()) {
			translator.registerModelInfo(options.modelInfoFile);
		}

		String content;
		try (InputStream is = new FileInputStream(options.cqlPath)) {
			content = IOUtils.toString(is, StandardCharsets.UTF_8);
		}

		// The values in the descriptor are not relevant for the translation CLI.
		CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
				.setFormat(Format.CQL)
				.setLibraryId("TranslationCLI")
				.setVersion("TranslationCLI");
		CqlLibrary library = new CqlLibrary()
				.setDescriptor(descriptor)
				.setContent(content);
		CqlTranslationResult result = translator.translate(library, new ProviderBasedCqlLibrarySourceProvider(libraryProvider));

		out.println("Translated Library: ");
		out.println(result.getMainLibrary().getContent());
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
