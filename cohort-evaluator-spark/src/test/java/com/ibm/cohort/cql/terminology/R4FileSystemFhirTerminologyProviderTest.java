/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.terminology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.hadoop.fs.Path;
import org.apache.spark.deploy.SparkHadoopUtil;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import org.junit.Assert;

@RunWith(Parameterized.class)
public class R4FileSystemFhirTerminologyProviderTest {
	private static final String TEST_DISPLAY = "Display";
	private static final String TEST_DISPLAY_FOR_VERSION2 = "Display for 2021-09";
	private static final String TEST_CODE = "10901-7";
	private static final String TEST_SYSTEM = "http://snomed.info/sct";
	private static final String TEST_SYSTEM_VERSION1 = "2020-09";
	private static final String TEST_SYSTEM_VERSION2 = "2021-09";
	private static final String TEST_CODE_MULTIPLE_CODE_SYSTEMS = "10901-8";
	private static R4FileSystemFhirTerminologyProvider provider = null;
	private String setId;
	
	public R4FileSystemFhirTerminologyProviderTest(String setId) {
		this.setId = setId;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		//Run tests using XML input and JSON input
		return Arrays.asList(new Object[][] {
			{ "urn:oid:Test"}, { "urn:oid:TestXml"}
		});
	}

	@BeforeClass
	public static void initializeProvider() throws IOException {
		provider =  new R4FileSystemFhirTerminologyProvider(new Path ("src/test/resources/fileSystemValueSets"), SparkHadoopUtil.get().conf());
	}

	@Test
	public void expandOperationReturnsCorrectCodesMoreThanZero() {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		ValueSet valueSet = new ValueSet();
		valueSet.setId("Test");
		valueSet.getExpansion().getContainsFirstRep().setSystem(TEST_SYSTEM).setCode(TEST_CODE);

		Iterable<Code> codes = provider.expand(info);

		List<Code> list = StreamSupport.stream(codes.spliterator(), false).collect(Collectors.toList());
		assertEquals(list.size(), 4);
		
		for(Code code : list) {
			if(code.getCode() == null || code.getCode().isEmpty()) {
				Assert.fail("Code not present");
			}
			if(code.getSystem() == null || code.getSystem().isEmpty()) {
				Assert.fail("CodeSystem not present");
			}
			if(code.getVersion() == null || code.getVersion().isEmpty()) {
				Assert.fail("Version not present");
			}
			if(code.getDisplay() == null || code.getDisplay().isEmpty()) {
				Assert.fail("Display not present");
			}
				
		}
	}

	@Test
	public void inOperationReturnsTrue() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setSystem(TEST_SYSTEM);
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}
	
	@Test
	public void inOperationReturnsTrueWithVersion1() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setSystem(TEST_SYSTEM);
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY);
		code.setVersion(TEST_SYSTEM_VERSION1);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}
	
	@Test
	public void inOperationReturnsTrueWithVersion2() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setSystem(TEST_SYSTEM);
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY_FOR_VERSION2);
		code.setVersion(TEST_SYSTEM_VERSION2);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}
	
	@Test
	public void inOperationReturnsTrueWithCodeOnly() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setCode(TEST_CODE);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}

	@Test
	public void inOperationReturnsFalse() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setSystem(TEST_SYSTEM);
		code.setCode("Bad_Code");
		code.setDisplay(TEST_DISPLAY);

		boolean result = provider.in(code, info);
		assertFalse(result);
	}
	
	@Test
	public void inOperationReturnsFalseWithOnlyCode() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setCode("Bad_Code");

		boolean result = provider.in(code, info);
		assertFalse(result);
	}
	
	@Test
	public void inOperationReturnsFalseWithBadCodeSystem() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setSystem("bad system");
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY);

		boolean result = provider.in(code, info);
		assertFalse(result);
	}

	@Test
	public void inOperationHandlesNullSystem() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void inOperationHandlesMultipleCodeSystemCode() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setCode(TEST_CODE_MULTIPLE_CODE_SYSTEMS);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void lookupOperationSuccess() throws Exception {
		CodeSystemInfo info = new CodeSystemInfo();
		info.setId(TEST_SYSTEM);
		info.setVersion(TEST_SYSTEM_VERSION1);

		Code code = new Code();
		code.setCode(TEST_CODE);
		code.setSystem(TEST_SYSTEM);
		code.setDisplay(TEST_DISPLAY);

		provider.lookup(code, info);
	}
}