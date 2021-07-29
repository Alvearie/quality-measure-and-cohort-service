/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.ibm.cohort.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

public class LibraryFormatTest {
	@Test
	public void testCQLIsSupportedMimeTypeSuccess() {
		assertTrue( LibraryFormat.isSupportedMimeType( "text/cql" ) );
	}
	
	@Test
	public void testJSONIsNotSupportedMimeTypeSuccess() {
		assertFalse( LibraryFormat.isSupportedMimeType( "application/json" ) );
	}
	
	@Test
	public void testCQLIsSupportedExtensionSuccess() { 
		assertTrue( LibraryFormat.isSupportedExtension( ".cql" ) );
	}
	
	@Test
	public void testXMLIsSupportedExtensionSuccess() { 
		assertTrue( LibraryFormat.isSupportedExtension( ".xml" ) );
	}
	
	@Test
	public void testJsonIsNotSupportedExtensionSuccess() { 
		assertFalse( LibraryFormat.isSupportedExtension( ".json" ) );
	}
	
	@Test
	public void testCqlExtensionIsValidPath() {
		assertTrue( LibraryFormat.isSupportedPath( Paths.get("/tmp", "dummy.cql" ) ) );
	}
	
	@Test
	public void testJsonExtensionIsNotValidPath() {
		assertFalse( LibraryFormat.isSupportedPath( Paths.get("/tmp", "dummy.json" ) ) );
	}
	
	@Test
	public void testLibraryFormatForCQLPathSuccess() {
		assertEquals( LibraryFormat.CQL, LibraryFormat.forPath( Paths.get("/tmp", "dummy.cql" ) ) );
	}
	
	@Test
	public void testLibraryFormatForXMLPathSuccess() {
		assertEquals( LibraryFormat.XML, LibraryFormat.forPath( Paths.get("/tmp", "dummy.xml" ) ) );
	}
	
	@Test
	public void testLibraryFormatForCQLStringSuccess() {
		assertEquals( LibraryFormat.CQL, LibraryFormat.forString( "dummy.cql" ) );
	}
	
	@Test
	public void testLibraryFormatForXMLStringSuccess() {
		assertEquals( LibraryFormat.XML, LibraryFormat.forString( "dummy.xml" ) );
	}
	
	@Test
	public void testForMimeTypeCQLSuccess() {
		assertEquals( LibraryFormat.CQL, LibraryFormat.forMimeType( "text/cql" ) );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testForMimeTypeJSONFailure() {
		LibraryFormat.forMimeType( "application/json" );
	}
}
