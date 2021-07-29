/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.translator.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Before;

import com.ibm.cohort.version.DefaultFilenameToVersionedIdentifierStrategy;

public class InJVMCqlTranslationProviderTest extends CqlTranslatorProviderTest {

	private class TestLibraryProvider implements LibrarySourceProvider {

		Map<VersionedIdentifier, Map<LibraryFormat, String>> sources = new HashMap<>();

		public TestLibraryProvider(ZipInputStream zipInputStream, String... searchPaths) throws IOException {

			ZipEntry ze;
			while ((ze = zipInputStream.getNextEntry()) != null) {
				if (!ze.isDirectory()) {
					boolean filter = false;
					if( ! ArrayUtils.isEmpty(searchPaths) ) {
						String prefix = "";

						int ch;
						if( (ch=ze.getName().lastIndexOf('/')) != -1 ) {
							prefix = ze.getName().substring(0, ch);
						}
						filter = ! ArrayUtils.contains(searchPaths, prefix);
					} else {
						filter = false;
					}

					if( ! filter ) {
						LibraryFormat format = LibraryFormat.forString(ze.getName());
						if( format != null ) {
							VersionedIdentifier id = new DefaultFilenameToVersionedIdentifierStrategy().filenameToVersionedIdentifier(ze.getName());

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							IOUtils.copy(zipInputStream, baos);
							Map<LibraryFormat, String> formats = sources.computeIfAbsent(id, key -> new HashMap<>());
							formats.put(format, baos.toString(StandardCharsets.UTF_8.name()));
						}
					}
				}
			}
		}

		public TestLibraryProvider(Path folderPath) throws IOException {
			if( ! folderPath.toFile().isDirectory() ) {
				throw new IllegalArgumentException("Path is not a directory");
			}

			Files.walkFileTree(folderPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path entry, BasicFileAttributes attrs) throws IOException {
					if( ! attrs.isDirectory() && LibraryFormat.isSupportedPath( entry ) ) {
							LibraryFormat sourceFormat = LibraryFormat.forPath( entry );
							if( sourceFormat != null ) {
								String filename = entry.getFileName().toString();
								VersionedIdentifier id = new DefaultFilenameToVersionedIdentifierStrategy().filenameToVersionedIdentifier(filename);

								Map<LibraryFormat, String> formats = sources.computeIfAbsent( id, key -> new HashMap<>() );

								String text = FileUtils.readFileToString(entry.toFile(), StandardCharsets.UTF_8);
								formats.put(sourceFormat, new String( text.getBytes() ) );
							}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		@Override
		public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {

			String library = sources.get(versionedIdentifier).get(LibraryFormat.CQL);
			return (library == null) ? null : new ByteArrayInputStream(library.getBytes());
		}

	}

	private InJVMCqlTranslationProvider translator;

	@Before
	public void setUp() {
		translator = new InJVMCqlTranslationProvider();
	}

	protected CqlTranslationProvider getTranslator() {
		return translator;
	}

	protected void prepareForZip(File zipFile) throws IOException {
		try(InputStream is = new FileInputStream(zipFile)) {
			ZipInputStream zipInputStream = new ZipInputStream(is);
			translator.addLibrarySourceProvider(new TestLibraryProvider(zipInputStream));
		}
	}

	protected void prepareForFolder(Path folder) throws IOException {
		translator.addLibrarySourceProvider(new TestLibraryProvider(folder));
	}

	@Override
	protected void registerModelInfo(File modelInfo) throws IOException {
		translator.convertAndRegisterModelInfo(modelInfo);
	}
}
