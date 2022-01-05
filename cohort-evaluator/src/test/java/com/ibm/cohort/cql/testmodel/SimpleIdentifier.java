/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.testmodel;

import com.ibm.cohort.annotations.Generated;

import java.util.Objects;

/**
 * A simple implementation of a FHIR identifier.
 * Used with {@link SimpleObject} to test objects that operate on
 * arbitrary resources.
 */
@Generated
public class SimpleIdentifier {

    private String value;
    private String system;

    public SimpleIdentifier() { }
    public SimpleIdentifier(String value, String system) {
        this.value = value;
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleIdentifier that = (SimpleIdentifier) o;
        return Objects.equals(value, that.value) && Objects.equals(system, that.system);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, system);
    }

    @Override
    public String toString() {
        return "SimpleIdentifier{" +
                "value='" + value + '\'' +
                ", system='" + system + '\'' +
                '}';
    }
}
