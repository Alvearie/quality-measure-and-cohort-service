/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cli;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

public class FileHelpers {
	public static boolean isZip(File file) {
		return file.isFile() && FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("zip");
	}
	
	public static boolean isZip(Path path) {
		return isZip(path.toFile());
	}
}
