package com.ibm.cohort.cql.functions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.ibm.cohort.datarow.model.DataRow;

public class AnyColumnTest {

    @Test
    public void testAnyColumnNoMatches() {
        DataRow row = spy(DataRow.class);
        Set<String> allFields = new HashSet<>();
        allFields.add("nonMatchingField1");
        allFields.add("nonMatchingField2");
        doReturn(allFields).when(row).getFieldNames();

        String prefix = "prefix";
        List<Object> actual = (List<Object>) AnyColumn.AnyColumn(row, prefix);
        assertThat(actual, empty());
    }

    @Test
    public void testAnyColumnHasMatches() {
        String matchingField1 = "matchingField1";
        String matchingField2 = "matchingField2";
        String nonMatchingField = "nonMatchingField";
        String expectedValue1 = "matchingValue1";
        String expectedValue2 = "matchingValue2";

        DataRow row = spy(DataRow.class);
        Set<String> allFields = new HashSet<>();
        allFields.add(matchingField1);
        allFields.add(matchingField2);
        allFields.add(nonMatchingField);
        doReturn(allFields).when(row).getFieldNames();
        doReturn(expectedValue1).when(row).getValue(matchingField1);
        doReturn(expectedValue2).when(row).getValue(matchingField2);
        doReturn("nonMatchingValue").when(row).getValue(nonMatchingField);

        String prefix = "matchingField";
        List<Object> actual = (List<Object>) AnyColumn.AnyColumn(row, prefix);
        assertThat(actual, containsInAnyOrder(expectedValue1, expectedValue2));
    }

    @Test
    public void testAnyColumnRegexNoMatches() {
        DataRow row = spy(DataRow.class);
        Set<String> allFields = new HashSet<>();
        allFields.add("matchingField1");
        allFields.add("matchingField2");
        allFields.add("nonMatchingField");
        doReturn(allFields).when(row).getFieldNames();
        doReturn("matchingValue1").when(row).getValue("matchingField1");
        doReturn("matchingValue2").when(row).getValue("matchingField2");
        doReturn("nonMatchingValue").when(row).getValue("nonMatchingField");

        String regex = "prefix[0-9]+";
        List<Object> actual = (List<Object>) AnyColumn.AnyColumnRegex(row, regex);
        assertThat(actual, empty());
    }

    @Test
    public void testAnyColumnRegexHasMatches() {
        String matchingField1 = "matchingField1";
        String matchingField2 = "matchingField2";
        String nonMatchingField = "nonMatchingField";
        String expectedValue1 = "matchingValue1";
        String expectedValue2 = "matchingValue2";

        DataRow row = spy(DataRow.class);
        Set<String> allFields = new HashSet<>();
        allFields.add(matchingField1);
        allFields.add(matchingField2);
        allFields.add(nonMatchingField);
        doReturn(allFields).when(row).getFieldNames();
        doReturn(expectedValue1).when(row).getValue(matchingField1);
        doReturn(expectedValue2).when(row).getValue(matchingField2);
        doReturn("nonMatchingValue").when(row).getValue(nonMatchingField);

        String regex = "matchingField[0-9]+";
        List<Object> actual = (List<Object>) AnyColumn.AnyColumnRegex(row, regex);
        assertThat(actual, containsInAnyOrder("matchingValue1", "matchingValue2"));
    }
}