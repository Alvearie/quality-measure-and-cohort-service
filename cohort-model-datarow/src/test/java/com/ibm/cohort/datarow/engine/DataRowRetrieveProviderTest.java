/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
    public static final String FIELD_GENDER_CODE = "gender_code";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_BIRTH_DATE = "birth_date";

    public static final String GENDER_FEMALE = "female";
    public static final String GENDER_MALE = "male";

    LocalDate now;
    
    DataRowRetrieveProvider retrieveProvider;
    TerminologyProvider termProvider;
    Map<String, Iterable<Object>> data;

    @Before
    public void setUp() {
        now = LocalDate.now();
        
        data = new HashMap<>();
        termProvider = mock(TerminologyProvider.class);
        retrieveProvider = new DataRowRetrieveProvider(data, termProvider);
    }

    @Test
    public void testRetrieveNoCodeFilterHasDataKnownDataType() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                null, null, null, null, null, null, null);
        assertEquals(people.size(), count(rows));
    }

    @Test
    public void testRetrieveNoCodeFilterUnknownDataType() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, "unknown", null, null,
                null, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByProvidedCodes_dataForCodeIsString() {
        String maleId = "789";
        
        List<Object> people = makePeopleTestData(maleId);
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList(GENDER_MALE).stream().map(this::code)
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
    public void testRetrieveFilterByProvidedCodes_noMatch() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("unknown").stream().map(this::code)
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByProvidedCodes_unknownDataType() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("unknown").stream().map(this::code)
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, "unknown", null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(0, count(rows));
    }
    
    @Test
    public void testRetrieveFilterByProvidedCodes_dataForCodeIsInt() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("8").stream().map(this::code)
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_AGE, codes, null, null, null, null, null);
        assertEquals(1, count(rows));
    }
    
    @Test
    public void testRetrieveFilterByProvidedCodes_dataForCodeIsNull() {
        List<Object> people = makePeopleTestData();
        people.add(person("999", null, 104));
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList("female").stream().map(this::code)
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(3, count(rows));
    }
    
    @Test
    public void testRetrieveFilterByProvidedCodes_codesHaveSystem_dataForCodeDoesNot() {
        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        List<Code> codes = Arrays.asList(GENDER_FEMALE).stream().map(this::code).map(c -> c.withSystem("http://snomed.info/sct"))
                .collect(Collectors.toList());

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, codes, null, null, null, null, null);
        assertEquals(0, count(rows));
    }

    @Test
    public void testRetrieveFilterByValueSet() {
        String valueSetId = "urn:oid:allowed-genders";
        List<Code> codes = Arrays.asList(GENDER_MALE).stream().map(this::code)
                .collect(Collectors.toList());
        when(termProvider.expand(argThat(a -> a.getId().equals(valueSetId)))).thenReturn(codes);

        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        Iterable<Object> rows = retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID, null, DATATYPE_PERSON, null,
                FIELD_GENDER, null, valueSetId, null, null, null, null);
        assertEquals(1, count(rows));
    }

    @Test
    public void testRetrieveFilterByValueSetUnknown() {
        String valueSetId = "urn:oid:allowed-genders";
        when(termProvider.expand(argThat(a -> a.getId().equals(valueSetId)))).thenReturn(null);

        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        assertThrows(IllegalArgumentException.class, () -> retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID,
                null, DATATYPE_PERSON, null, FIELD_GENDER, null, valueSetId, null, null, null, null));
    }
    
    @Test
    public void testRetrieveFilterByDateFilteringUnsupported() {
        String valueSetId = "urn:oid:allowed-genders";
        when(termProvider.expand(argThat(a -> a.getId().equals(valueSetId)))).thenReturn(null);

        List<Object> people = makePeopleTestData();
        data.put(DATATYPE_PERSON, people);

        // datePath
        assertThrows(UnsupportedOperationException.class, () -> retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID,
                null, DATATYPE_PERSON, null, FIELD_GENDER, null, valueSetId, FIELD_BIRTH_DATE, null, null, null));
        
        // dateLowPath
        assertThrows(UnsupportedOperationException.class, () -> retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID,
                null, DATATYPE_PERSON, null, FIELD_GENDER, null, valueSetId, null, FIELD_BIRTH_DATE, null, null));
        
        // dateHighPath
        assertThrows(UnsupportedOperationException.class, () -> retrieveProvider.retrieve(CONTEXT_CLAIM, FIELD_PERSON_ID,
                null, DATATYPE_PERSON, null, FIELD_GENDER, null, valueSetId, null, null, FIELD_BIRTH_DATE, null));
    }
    
    protected List<Object> makePeopleTestData() {
        return makePeopleTestData("789");
    }

    protected List<Object> makePeopleTestData(String maleId) {
        List<Object> people = new ArrayList<>();
        people.add(person("123", GENDER_FEMALE, 65));
        people.add(person("456", GENDER_FEMALE, 42));
        people.add(person("789", GENDER_MALE, 43));
        people.add(person("012", GENDER_FEMALE, 8));
        return people;
    }
    
    protected DataRow person(String id, String gender, int age) {
        Map<String, Object> person = new HashMap<>();
        person.put(FIELD_PERSON_ID, id);
        person.put(FIELD_GENDER, gender);
        person.put(FIELD_GENDER_CODE, code(gender));
        person.put(FIELD_AGE, age);
        person.put(FIELD_BIRTH_DATE, now.minusYears(age));
        return new SimpleDataRow(person);
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