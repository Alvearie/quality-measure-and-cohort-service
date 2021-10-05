package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.util.Objects;

public class AnyStringSinglePOJO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String primaryField;
    private String otherField;

    public String getPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(String primaryField) {
        this.primaryField = primaryField;
    }

    public String getOtherField() {
        return otherField;
    }

    public void setOtherField(String otherField) {
        this.otherField = otherField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyStringSinglePOJO that = (AnyStringSinglePOJO) o;
        return Objects.equals(primaryField, that.primaryField) &&
            Objects.equals(otherField, that.otherField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryField, otherField);
    }
}
