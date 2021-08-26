package com.ibm.cohort.cql.spark.aggregation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes({
    @JsonSubTypes.Type( value = ManyToMany.class, name="ManyToMany"),
    @JsonSubTypes.Type( value = OneToMany.class, name="OneToMany"),
})
public abstract class Join {
    private String primaryDataTypeColumn;
    private String relatedDataType;
    private String relatedKeyColumn;
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
}
