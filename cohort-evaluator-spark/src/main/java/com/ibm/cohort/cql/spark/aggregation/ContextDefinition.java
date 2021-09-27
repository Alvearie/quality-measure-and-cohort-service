/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ContextDefinition {
    private String name;
    private String primaryDataType;
    private String primaryKeyColumn;
    
    private List<Join> relationships;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryDataType() {
        return primaryDataType;
    }

    public void setPrimaryDataType(String primaryDataType) {
        this.primaryDataType = primaryDataType;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public List<Join> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Join> relationships) {
        this.relationships = relationships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ContextDefinition that = (ContextDefinition) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(primaryDataType, that.primaryDataType)
                .append(primaryKeyColumn, that.primaryKeyColumn)
                .append(relationships, that.relationships)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(primaryDataType)
                .append(primaryKeyColumn)
                .append(relationships)
                .toHashCode();
    }
}
