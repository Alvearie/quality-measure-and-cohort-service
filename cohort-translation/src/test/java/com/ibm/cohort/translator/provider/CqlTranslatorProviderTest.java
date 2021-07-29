/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.translator.provider;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.cql_annotations.r1.CqlToElmInfo;
import org.hl7.cql_annotations.r1.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;


/**
 * Baseline tests that can be run against multiple CqlTranslatorWrapper
 * implementations. The prepare methods provide call out points for
 * implementations that have specific needs based on the source
 * of the input data.
 */
public abstract class CqlTranslatorProviderTest {

	protected abstract CqlTranslationProvider getTranslator();

	protected abstract void prepareForZip(File zipFile) throws IOException;

	protected abstract void prepareForFolder(Path folder) throws IOException;


	@Test
	public void multipleFilesInZip__translatedSuccessfully() throws Exception {

		List<Library> libraries = new ArrayList<>();

		File testdata = new File("src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");
		prepareForZip(testdata);

		try( ZipFile zipFile = new ZipFile(testdata) ) {
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

		assertEquals(1, libraries.size());
		Library library = libraries.get(0);
		assertEquals(1, library.getAnnotation().size());
		assertEquals("BreastCancerScreening", library.getIdentifier().getId());
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

		List<Object> unmarshalled = unmarshallAnnotations(library);
		Object o = unmarshalled.get(0);
		assertTrue( o instanceof CqlToElmInfo );
		CqlToElmInfo info = (CqlToElmInfo) o;
		assertEquals("EnableAnnotations,EnableLocators,DisableListDemotion,DisableListPromotion", info.getTranslatorOptions());
	}

	@Test
	public void errorCausedByInvalidContent() {
		boolean failed = false;
		String content = "junk";
		try (InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			getTranslator().translate(is);
		}
		catch (Exception e) {
			failed = true;
			Assert.assertTrue(
					"Unexpected exception message: " + e.getMessage(),
					e.getMessage().startsWith("CQL translation contained errors:")
			);
		}
		if (!failed) {
			Assert.fail("Did not fail translation");
		}
	}

	@Test
	public void exceptionCausedByNotIncludingFHIRHelpers() {
		boolean failed = false;
		Path cqlFile = Paths.get("src/test/resources/cql/failure/exception.cql");
		try (InputStream is = Files.newInputStream(cqlFile)) {
			getTranslator().translate(is);
		}
		catch (Exception e) {
			failed = true;
			Assert.assertTrue(
					"Unexpected exception message: " + e.getMessage(),
					e.getMessage().startsWith("CQL translation contained exceptions:")
			);
		}
		if (!failed) {
			Assert.fail("Did not fail translation");
		}
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

	public static List<Object> unmarshallAnnotations(Library library) throws Exception {

		List<Object> annotations = new ArrayList<>();
		if (library.getAnnotation() != null) {
			JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller u = ctx.createUnmarshaller();

			for (Object elem : library.getAnnotation()) {
				JAXBElement<?> j = (JAXBElement<?>) u.unmarshal((Element) elem);
				annotations.add(j.getValue());
			}
		}
		return annotations;
	}
}
