/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.nio.file.Path;
import java.util.zip.ZipFile;

import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.junit.Before;

public class InJVMCqlTranslationProviderTest extends CqlTranslatorWrapperTest {

	private InJVMCqlTranslationProvider translator;
	
	@Before
	public void setUp() {
		translator = new InJVMCqlTranslationProvider();
	}
	
	protected CqlTranslationProvider getTranslator() {
		return translator;
	}

	protected void prepareForZip(ZipFile zipFile) throws Exception {
		translator.addLibrarySourceProvider(new ZipLibrarySourceProvider(zipFile));
	}
	
	protected void prepareForFolder(Path folder) throws Exception {
		translator.addLibrarySourceProvider(new DefaultLibrarySourceProvider(folder));
	}
}
