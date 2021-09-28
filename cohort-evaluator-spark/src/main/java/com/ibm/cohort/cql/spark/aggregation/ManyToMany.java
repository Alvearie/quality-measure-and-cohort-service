/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ManyToMany extends Join {
    private String associationDataType;
    private String associationOneKeyColumn;
    private String associationManyKeyColumn;
    
    public String getAssociationDataType() {
        return associationDataType;
    }
    public void setAssociationDataType(String associationDataType) {
        this.associationDataType = associationDataType;
    }
    public String getAssociationOneKeyColumn() {
        return associationOneKeyColumn;
    }
    public void setAssociationOneKeyColumn(String associationOneKeyColumn) {
        this.associationOneKeyColumn = associationOneKeyColumn;
    }
    public String getAssociationManyKeyColumn() {
        return associationManyKeyColumn;
    }
    public void setAssociationManyKeyColumn(String associationManyKeyColumn) {
        this.associationManyKeyColumn = associationManyKeyColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ManyToMany that = (ManyToMany) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(associationDataType, that.associationDataType)
                .append(associationOneKeyColumn, that.associationOneKeyColumn)
                .append(associationManyKeyColumn, that.associationManyKeyColumn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(associationDataType)
                .append(associationOneKeyColumn)
                .append(associationManyKeyColumn)
                .toHashCode();
    }
}
