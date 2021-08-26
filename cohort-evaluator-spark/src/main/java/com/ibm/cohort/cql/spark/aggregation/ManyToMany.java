package com.ibm.cohort.cql.spark.aggregation;

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
}
