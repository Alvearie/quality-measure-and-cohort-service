package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.util.Objects;

public class AnyStringMultiplePOJO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String anyPrimary;
    private String any2;
    private String any3;

    public AnyStringMultiplePOJO(String anyPrimary, String any2, String any3) {
        this.anyPrimary = anyPrimary;
        this.any2 = any2;
        this.any3 = any3;
    }

    public String getAnyPrimary() {
        return anyPrimary;
    }

    public void setAnyPrimary(String anyPrimary) {
        this.anyPrimary = anyPrimary;
    }

    public String getAny2() {
        return any2;
    }

    public void setAny2(String any2) {
        this.any2 = any2;
    }

    public String getAny3() {
        return any3;
    }

    public void setAny3(String any3) {
        this.any3 = any3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyStringMultiplePOJO that = (AnyStringMultiplePOJO) o;
        return Objects.equals(anyPrimary, that.anyPrimary) &&
            Objects.equals(any2, that.any2) &&
            Objects.equals(any3, that.any3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anyPrimary, any2, any3);
    }
}
