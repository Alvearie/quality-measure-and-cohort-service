/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HadoopBasedCqlLibraryProvider implements CqlLibraryProvider {

	private Path directory;
	private Configuration configuration;

	public HadoopBasedCqlLibraryProvider(Path directory, Configuration configuration) {
		this.directory = directory;
		this.configuration = configuration;
	}

	@Override
	public CqlLibrary getLibrary(CqlLibraryDescriptor libraryDescriptor) {
		CqlLibrary library = null;

		try {
			FileSystem fileSystem = directory.getFileSystem(configuration);
			Path path = new Path(directory, new Path(CqlLibraryHelpers.libraryDescriptorToFilename(libraryDescriptor)));
			if (fileSystem.exists(path)) {
				try (FSDataInputStream f = fileSystem.open(path)) {
					library = new CqlLibrary()
							.setDescriptor(libraryDescriptor)
							.setContent(IOUtils.toString(f, Charset.defaultCharset()));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to deserialize library " + libraryDescriptor, e);
		}

		return library;
	}
}