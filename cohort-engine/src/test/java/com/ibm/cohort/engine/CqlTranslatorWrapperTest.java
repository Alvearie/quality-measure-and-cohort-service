/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.cql_annotations.r1.CqlToElmInfo;
import org.junit.Test;

/**
 * Baseline tests that can be run against multiple CqlTranslatorWrapper
 * implementations. The prepare methods provide call out points for 
 * implementations that have specific needs based on the source
 * of the input data. 
 */
public abstract class CqlTranslatorWrapperTest {

	protected abstract CqlTranslationProvider getTranslator();

	protected abstract void prepareForZip(ZipFile zipFile) throws Exception;
	
	protected abstract void prepareForFolder(Path folder) throws Exception;

	
	@Test
	public void multipleFilesInZip__translatedSuccessfully() throws Exception {

		List<Library> libraries = new ArrayList<Library>();

		File testdata = new File("src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");
		try( ZipFile zipFile = new ZipFile(testdata) ) {
			prepareForZip(zipFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".cql")) {
					try (InputStream is = zipFile.getInputStream(entry)) {
						libraries.add(getTranslator().translate(is, null));
					}
				}
			}
		}
		
		assertEquals(2, libraries.size());
		Library library = libraries.get(0);
		assertEquals(1, library.getAnnotation().size());
		assertEquals("Breast-Cancer-Screening", library.getIdentifier().getId());
	}

	@Test
	public void multipleFilesInFolder__translatedSuccessfully() throws Exception {
		List<Library> libraries = new ArrayList<Library>();

		Path testdata = Paths.get("src/test/resources/cql/zip");
		prepareForFolder(testdata);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testdata, "*.cql")) {
			for (Path entry : stream) {
				try (InputStream cql = Files.newInputStream(entry)) {
					libraries.add(getTranslator().translate(cql, null));
				}
			}
		}
		assertEquals(2, libraries.size());
		Library library = getById(libraries, "Breast-Cancer-Screening");
		assertNotNull(library);
	}

	@Test
	public void singleFile_withOptions__translatedSuccessfully() throws Exception {
		List<Library> libraries = new ArrayList<Library>();

		Path testdata = Paths.get("src/test/resources/cql/basic");
		prepareForFolder(testdata);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testdata, "*.cql")) {
			for (Path entry : stream) {
				try (InputStream cql = Files.newInputStream(entry)) {
					List<Options> defaultOptions = new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions());
					libraries.add(getTranslator().translate(cql, defaultOptions));
				}
			}
		}
		assertEquals(1, libraries.size());
		Library library = libraries.get(0);
		assertEquals("Test", library.getIdentifier().getId());
		assertEquals(1, library.getAnnotation().size());

		List<Object> unmarshalled = LibraryUtils.unmarshallAnnotations(library);
		Object o = unmarshalled.get(0);
		assertTrue( o instanceof CqlToElmInfo );
		CqlToElmInfo info = (CqlToElmInfo) o;
		assertEquals("EnableAnnotations,EnableLocators,DisableListDemotion,DisableListPromotion", info.getTranslatorOptions());
	}
	
	private Library getById(List<Library> libraries, String libraryName) {
		Library library = null;
		for( Library l : libraries ) {
			if( l.getIdentifier().getId().equals(libraryName) ) {
				library = l;
				break;
			}
		}
		return library;
	}
}
