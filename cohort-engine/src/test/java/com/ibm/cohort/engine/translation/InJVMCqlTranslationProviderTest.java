/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import org.junit.Before;

import com.ibm.cohort.engine.DirectoryLibrarySourceProvider;
import com.ibm.cohort.engine.ZipStreamLibrarySourceProvider;

public class InJVMCqlTranslationProviderTest extends CqlTranslatorProviderTest {

	private InJVMCqlTranslationProvider translator;
	
	@Before
	public void setUp() {
		translator = new InJVMCqlTranslationProvider();
	}
	
	protected CqlTranslationProvider getTranslator() {
		return translator;
	}

	protected void prepareForZip(File zipFile) throws IOException  {
		try (InputStream is = new FileInputStream(zipFile)) {
			ZipInputStream zis = new ZipInputStream(is);
			translator.addLibrarySourceProvider(new ZipStreamLibrarySourceProvider(zis));
		}
	}
	
	protected void prepareForFolder(Path folder) throws IOException {
		translator.addLibrarySourceProvider(new DirectoryLibrarySourceProvider(folder));
	}
}
