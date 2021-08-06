/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.cohort.datarow.model.DataRow;
import com.ibm.cohort.datarow.model.SimpleDataRow;

public class DataRowRetrieveProviderTest {

    public static final String CONTEXT_CLAIM = "CLAIM";
    public static final String CONTEXT_ADMIT = "ADMIT";

    public static final String DATATYPE_PERSON = "PERSON";

    public static final String FIELD_PERSON_ID = "person_id";
    public static final String FIELD_GENDER = "gender";

    public static final String GENDER_FEMALE = "female";
    public static final String GENDER_MALE = "male";

    DataRowRetrieveProvider retrieveProvider;
    TerminologyProvider termProvider;
    Map<String, Iterable<Object>> data;

    @Before
    public void setUp() {
        data = new HashMap<>();
        termProvider = mock(TerminologyProvider.class);
        retrieveProvider = new DataRowRetrieveProvider(data, termProvider);
    }

    @Test
    public void testRetrieveNoCodeFilterHasDataKnownDataType() {
        List<Object> people = new ArrayList<>();
        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));
        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                null, null, null, null, null, null, null);
        assertEquals(people.size(), count(rows));
    }

    @Test
    public void testRetrieveNoCodeFilterUnknownDataType() {
        List<Object> people = new ArrayList<>();
        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));
        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, "unknown", null, null,
                null, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByProvidedCodes() {
        List<Object> people = new ArrayList<>();

        String maleId = "789";

        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person(maleId, GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));

        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList(GENDER_MALE).stream().map(g -> new Code().withCode(g))
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        int count = 0;
        for (Object obj : rows) {
            count++;
            DataRow actual = (DataRow) obj;
            assertEquals(maleId, actual.getValue(FIELD_PERSON_ID));
        }
        assertEquals(1, count);
    }

    @Test
    public void testRetrieveFilterByCQLCodeColumn() {
        List<Object> people = new ArrayList<>();

        String maleId = "789";

        people.add(person("123", code(GENDER_FEMALE)));
        people.add(person("456", code(GENDER_FEMALE)));
        people.add(person(maleId, code(GENDER_MALE)));
        people.add(person("012", code(GENDER_FEMALE)));

        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList(GENDER_MALE).stream().map(g -> new Code().withCode(g))
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        int count = 0;
        for (Object obj : rows) {
            count++;
            DataRow actual = (DataRow) obj;
            assertEquals(maleId, actual.getValue(FIELD_PERSON_ID));
        }
        assertEquals(1, count);
    }

    @Test
    public void testRetrieveFilterByProvidedCodesNoMatch() {
        List<Object> people = new ArrayList<>();

        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));

        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("unknown").stream().map(g -> new Code().withCode(g))
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByProvidedCodesUnknownDataType() {
        List<Object> people = new ArrayList<>();

        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));

        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("unknown").stream().map(g -> new Code().withCode(g))
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, "unknown", null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByValueSet() {
        String valueSetId = "urn:oid:allowed-genders";
        List<Code> codes = Arrays.asList(GENDER_MALE).stream().map(g -> new Code().withCode(g))
                .collect(Collectors.toList());
        when(termProvider.expand(argThat(a -> a.getId().equals(valueSetId)))).thenReturn(codes);

        List<Object> people = new ArrayList<>();

        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));

        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, null, valueSetId, null, null, null, null);
        assertEquals(1, count(rows));
    }

    @Test
    public void testRetrieveFilterByValueSetUnknown() {
        String valueSetId = "urn:oid:allowed-genders";
        when(termProvider.expand(argThat(a -> a.getId().equals(valueSetId)))).thenReturn(null);

        List<Object> people = new ArrayList<>();

        people.add(person("123", GENDER_FEMALE));
        people.add(person("456", GENDER_FEMALE));
        people.add(person("789", GENDER_MALE));
        people.add(person("012", GENDER_FEMALE));

        data.put(DATATYPE_PERSON, people);

        assertThrows(IllegalArgumentException.class, () -> retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID,
                null, DATATYPE_PERSON, null, FIELD_GENDER, null, valueSetId, null, null, null, null));
    }

    protected DataRow person(String id, Object gender) {
        DataRow row;
        Map<String, Object> female = new HashMap<>();
        female.put(FIELD_PERSON_ID, id);
        female.put(FIELD_GENDER, gender);
        row = new SimpleDataRow(female);
        return row;
    }

    protected Code code(String code) {
        return new Code().withCode(code);
    }

    protected int count(Iterable<Object> rows) {
        int count = 0;
        Iterator<Object> it;
        for (it = rows.iterator(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }
}