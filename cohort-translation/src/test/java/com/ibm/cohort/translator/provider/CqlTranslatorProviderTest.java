/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.translator.provider;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;


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

	protected abstract void registerModelInfo(File modelInfo) throws IOException;
	protected abstract void registerModelInfo(InputStream modelInfo) throws IOException;
	protected abstract void registerModelInfo(Reader modelInfo) throws IOException;


	@Test
	public void multipleFilesInZip__translatedSuccessfully() throws Exception {

		List<String> libraries = new ArrayList<>();

		File testdata = new File("src/test/resources/cql/zip/breast_cancer_screening_v1_0_0_cql.zip");
		prepareForZip(testdata);
		registerModelInfo(new File("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml"));

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
	}

	@Test
	public void multipleFilesInFolder__translatedSuccessfully() throws Exception {
		List<String> libraries = new ArrayList<>();

		Path testdata = Paths.get("src/test/resources/cql/zip");
		prepareForFolder(testdata);
		registerModelInfo(new FileInputStream(new File("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml")));

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testdata, "*.cql")) {
			for (Path entry : stream) {
				try (InputStream cql = Files.newInputStream(entry)) {
					libraries.add(getTranslator().translate(cql, null));
				}
			}
		}
		assertEquals(2, libraries.size());
	}

	@Test
	public void singleFile_withOptions__translatedSuccessfully() throws Exception {
		List<String> libraries = new ArrayList<>();

		Path testdata = Paths.get("src/test/resources/cql/basic");
		prepareForFolder(testdata);
		registerModelInfo(new InputStreamReader(new FileInputStream(new File("src/test/resources/modelinfo/ig-with-target-modelinfo-0.0.1.xml"))));

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(testdata, "*.cql")) {
			for (Path entry : stream) {
				try (InputStream cql = Files.newInputStream(entry)) {
					List<Options> defaultOptions = new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions());
					libraries.add(getTranslator().translate(cql, defaultOptions));
				}
			}
		}
		assertEquals(1, libraries.size());
		
		JAXBContext ctx = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] { ObjectFactory.class }, null);
		Unmarshaller u = ctx.createUnmarshaller();
		
		String elm;
		elm = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("cql/basic/test.xml"), "utf-8");
		Library expectedLibrary = readLibrary(u, elm);
		
		elm = libraries.get(0);
		Library actualLibrary = readLibrary(u, elm);
		
		assertEquals(expectedLibrary.getIdentifier(), actualLibrary.getIdentifier());
		assertEquals(expectedLibrary.getAnnotation().size(), actualLibrary.getAnnotation().size());
		assertEquals(expectedLibrary.getStatements().getDef().size(), actualLibrary.getStatements().getDef().size());

	}

	protected Library readLibrary(Unmarshaller u, String elm)
			throws JAXBException {
		JAXBElement<Library> e = u.unmarshal(new StreamSource(new StringReader(elm)), Library.class);
		return e.getValue();
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
}
