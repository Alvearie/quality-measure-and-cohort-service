package com.ibm.cohort.cql.functions;

import java.util.stream.Collectors;

import com.ibm.cohort.datarow.model.DataRow;

public class AnyColumn {

    private AnyColumn() {
    }

    public static Object AnyColumn(Object object, String fieldPrefix) {
        DataRow dataRow = (DataRow) object;

        return dataRow.getFieldNames().stream().filter(fieldName -> fieldName.startsWith(fieldPrefix))
            .map(dataRow::getValue)
            .collect(Collectors.toList());
    }

    public static Object AnyColumnRegex(Object object, String regex) {
        DataRow dataRow = (DataRow) object;

        return dataRow.getFieldNames().stream().filter(fieldName -> fieldName.matches(regex))
            .map(dataRow::getValue)
            .collect(Collectors.toList());
    }
}
