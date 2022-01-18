/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.aggregation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes({
    @JsonSubTypes.Type( value = ManyToMany.class, name="ManyToMany"),
    @JsonSubTypes.Type( value = MultiManyToMany.class, name="MultiManyToMany"),
    @JsonSubTypes.Type( value = OneToMany.class, name="OneToMany"),
})
public abstract class Join {
    private String primaryDataTypeColumn;
    private String relatedDataType;
    private String relatedKeyColumn;
    private String whereClause;
    private int minRows = 0;
    private int maxRows = -1;
    
    
    public String getPrimaryDataTypeColumn() {
        return primaryDataTypeColumn;
    }
    public void setPrimaryDataTypeColumn(String primaryDataTypeColumn) {
        this.primaryDataTypeColumn = primaryDataTypeColumn;
    }
    public String getRelatedDataType() {
        return relatedDataType;
    }
    public void setRelatedDataType(String relatedDataType) {
        this.relatedDataType = relatedDataType;
    }
    public String getRelatedKeyColumn() {
        return relatedKeyColumn;
    }
    public void setRelatedKeyColumn(String relatedKeyColumn) {
        this.relatedKeyColumn = relatedKeyColumn;
    }
    public int getMinRows() {
        return minRows;
    }
    public void setMinRows(int minRows) {
        this.minRows = minRows;
    }
    public int getMaxRows() {
        return maxRows;
    }
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
    public String getWhereClause() {
        return whereClause;
    }
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Join join = (Join) o;

        return new EqualsBuilder()
                .append(minRows, join.minRows)
                .append(maxRows, join.maxRows)
                .append(primaryDataTypeColumn, join.primaryDataTypeColumn)
                .append(relatedDataType, join.relatedDataType)
                .append(relatedKeyColumn, join.relatedKeyColumn)
                .append(whereClause, join.whereClause)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(primaryDataTypeColumn)
                .append(relatedDataType)
                .append(relatedKeyColumn)
                .append(minRows)
                .append(maxRows)
                .append(whereClause)
                .toHashCode();
    }
}
