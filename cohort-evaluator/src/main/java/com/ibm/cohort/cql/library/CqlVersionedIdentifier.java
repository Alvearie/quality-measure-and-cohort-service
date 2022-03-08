/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import java.io.Serializable;
import java.util.Objects;

public class CqlVersionedIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String version;

    public CqlVersionedIdentifier(String id, String version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CqlVersionedIdentifier that = (CqlVersionedIdentifier) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "CqlVersionedIdentifier{" +
            "id='" + id + '\'' +
            ", version='" + version + '\'' +
            '}';
    }
}
