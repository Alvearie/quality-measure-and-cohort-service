package com.ibm.cohort.cql.spark.aggregation;

import java.util.List;

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
}
