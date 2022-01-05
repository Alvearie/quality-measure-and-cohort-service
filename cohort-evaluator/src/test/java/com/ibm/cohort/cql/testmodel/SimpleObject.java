/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.testmodel;

import com.ibm.cohort.annotations.Generated;

import java.util.List;
import java.util.Objects;

/**
 * A simple object that contains the fields commonly used in FHIR knowledge artifacts.
 * Intended to assist with testing objects that operate on arbitrary resources.
 * Uses {@link SimpleIdentifier} as its identifier object.
 */
@Generated
public class SimpleObject {

    private String id;
    private String version;
    private String name;
    private String url;
    private List<SimpleIdentifier> identifiers;

    public SimpleObject() { }
    public SimpleObject(String id, String version, String name, String url, List<SimpleIdentifier> identifiers) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.url = url;
        this.identifiers = identifiers;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<SimpleIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<SimpleIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleObject that = (SimpleObject) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version) && Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(identifiers, that.identifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, name, url, identifiers);
    }

    @Override
    public String toString() {
        return "SimpleObject{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", identifiers=" + identifiers +
                '}';
    }
}
