/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;

public class CodeKeyTest {
    @Test
    public void testUseCodeAsMapKeyFails() {
        Map<Object, String> map = new HashMap<>();
        map.put(new Code().withCode("123"), "123");
        map.put(new Code().withCode("456"), "456");
        map.put(new Code().withCode("789"), "789");
        map.put(new Code().withCode("123").withSystem("http://snomed.info/sct"), "SNOMED");
        map.put(new Code().withCode("123").withSystem("http://snomed.info/sct").withDisplay("display"), "Display");

        assertEquals(5, map.size());
        String value = map.get(new Code().withCode("123"));
        assertNull("Lookup by Code shouldn't work", value);
    }

    @Test
    public void testUseCodeKeyAsMapKeySucceeds() {
        Map<Object, String> map = new HashMap<>();
        map.put(new CodeKey().withSystem("http://snomed.info/sct"), "Only System");
        map.put(new CodeKey().withCode("123"), "123");
        map.put(new CodeKey().withCode("456"), "456");
        map.put(new CodeKey().withCode("789"), "789");
        map.put(new CodeKey().withCode("123").withSystem("http://snomed.info/sct"), "SNOMED");
        map.put(new CodeKey().withCode("123").withSystem("http://snomed.info/sct").withDisplay("display"), "Display");

        assertEquals(6, map.size());
        String value = map.get(new CodeKey().withCode("123"));
        assertNotNull("Lookup by CodeKey failed", value);
        assertEquals("123", value);
    }

    @Test
    public void testCodeKeyMirrorsCodeFields() {
        Code expected = new Code().withCode("123").withSystem("http://snomed.info/sct").withDisplay("display")
                .withVersion("20200809");
        CodeKey actual = new CodeKey(expected);

        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getSystem(), actual.getSystem());
        assertEquals(expected.getDisplay(), actual.getDisplay());
        assertEquals(expected.getVersion(), actual.getVersion());
    }

    @Test
    public void testCodeKeysAreEqual() {
        Code data = new Code().withCode("123").withSystem("http://snomed.info/sct").withDisplay("display")
                .withVersion("20200809");
        CodeKey left = new CodeKey(data);
        CodeKey right = new CodeKey(data);

        assertEquals(left, right);
    }

    @Test
    public void testCodeKeysNotEqualMissingSystem() {
        Code data = new Code().withCode("123").withSystem("http://snomed.info/sct").withDisplay("display")
                .withVersion("20200809");
        CodeKey codeKeyWithSystem = new CodeKey(data);
        
        CodeKey codeKeyWithoutSystem = new CodeKey(data.withSystem(null));

        assertNotEquals(codeKeyWithSystem, codeKeyWithoutSystem);
    }
}
