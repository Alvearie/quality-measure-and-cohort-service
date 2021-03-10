/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.measure;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import ca.uhn.fhir.parser.IParser;

/**
 * Implementation of a FHIR knowledge artifact resource resolution provider that is based
 * on data in a zip file.
 */
public class ZipResourceResolutionProvider extends ResourceResolutionProvider {

	public ZipResourceResolutionProvider(ZipFile zipFile, IParser parser, String... searchPaths) throws IOException {
		
		Enumeration<? extends ZipEntry> en = zipFile.entries();
		while( en.hasMoreElements() ) {
			ZipEntry entry = en.nextElement();
			
			if( isFileAllowed(entry.getName(), searchPaths) ) {
				processResource(entry.getName(), zipFile.getInputStream(entry), parser);
			}
		}	
	}
	
	public ZipResourceResolutionProvider(ZipInputStream zis, IParser parser, String... searchPaths) throws IOException {
		
		ZipEntry entry;
		while( (entry = zis.getNextEntry()) != null ) {
			if( isFileAllowed(entry.getName(), searchPaths) ) {
				processResource(entry.getName(), zis, parser);
			}
		}	
	}

}
