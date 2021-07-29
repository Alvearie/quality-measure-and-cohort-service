///*
// * (C) Copyright IBM Corp. 2020, 2020
// *
// * SPDX-License-Identifier: Apache-2.0
// */
//
//package com.ibm.cohort.engine;
//
//import static org.junit.Assert.assertEquals;
//
//import org.hl7.elm.r1.VersionedIdentifier;
//import org.junit.Before;
//import org.junit.Test;
//
//public class DefaultFilenameToVersionedIdentifierStrategyTest {
//
//	private DefaultFilenameToVersionedIdentifierStrategy converter;
//
//	@Before
//	public void setUp() {
//		this.converter = new DefaultFilenameToVersionedIdentifierStrategy();
//	}
//
//	@Test
//	public void testSimpleValidFilenameConversion() {
//		runTest( "Corey-1.cql", "Corey", "1");
//	}
//
//	@Test
//	public void testMultiDashValidFilenameConversion() {
//		runTest( "Corey-Is-Here-1.cql", "Corey-Is-Here", "1");
//	}
//
//	@Test
//	public void testTrailingDashValidFilenameConversion() {
//		runTest( "Corey-Is-Here-.cql", "Corey-Is-Here-", null);
//	}
//
//	@Test
//	public void testDottedVersionValidFilenameConversion() {
//		runTest( "Corey-Is-Here-1.0.0.cql", "Corey-Is-Here", "1.0.0");
//	}
//
//	@Test
//	public void testDottedVersionValidFilenameConversion2() {
//		runTest( "TestUOMCompare-1.0.0.cql", "TestUOMCompare", "1.0.0");
//	}
//
//	@Test
//	public void testNoExtensionWithDotsValidFilenameConversion() {
//		runTest( "Corey-Is-Here-1.0.0", "Corey-Is-Here", "1.0");
//	}
//
//	@Test
//	public void testNoExtensionWithoutDotsValidFilenameConversion() {
//		runTest( "Corey-Is-Here-1", "Corey-Is-Here", "1");
//	}
//
//	private VersionedIdentifier runTest(String filename, String libraryId, String libraryVersion) {
//		VersionedIdentifier vid = converter.filenameToVersionedIdentifier(filename);
//		assertEquals( libraryId, vid.getId() );
//		assertEquals( libraryVersion, vid.getVersion() );
//		return vid;
//	}
//}
