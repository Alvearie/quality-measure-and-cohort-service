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

@RunWith(Parameterized.class)
public class R4FileSystemFhirTerminologyProviderTest {
	private static final String TEST_DISPLAY = "Display";
	private static final String TEST_CODE = "10901-7";
	private static final String TEST_SYSTEM = "http://snomed.info/sct";
	private static final String TEST_SYSTEM_VERSION = "2020-09";
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
		assertEquals(list.size(), 1);
		assertEquals(list.get(0).getSystem(), TEST_SYSTEM);
		assertEquals(list.get(0).getCode(), TEST_CODE);
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
	public void inOperationHandlesNullSystem() throws Exception {
		ValueSetInfo info = new ValueSetInfo();
		info.setId(setId);

		Code code = new Code();
		code.setCode(TEST_CODE);
		code.setDisplay(TEST_DISPLAY);

		boolean result = provider.in(code, info);
		assertTrue(result);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void lookupOperationSuccess() throws Exception {
		CodeSystemInfo info = new CodeSystemInfo();
		info.setId(TEST_SYSTEM);
		info.setVersion(TEST_SYSTEM_VERSION);

		Code code = new Code();
		code.setCode(TEST_CODE);
		code.setSystem(TEST_SYSTEM);
		code.setDisplay(TEST_DISPLAY);

		provider.lookup(code, info);
	}
}