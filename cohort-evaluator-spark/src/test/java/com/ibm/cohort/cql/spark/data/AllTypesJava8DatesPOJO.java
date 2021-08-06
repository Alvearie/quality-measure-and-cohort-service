/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AllTypesJava8DatesPOJO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final Random random = new Random();
    
    private String stringField;
    private Boolean booleanField;
    
    private LocalDate localDateField;
    private Instant instantField;
    
    private Integer integerField;
    private Short shortField;
    private Byte byteField;
    private Long longField;
    
    private Float floatField;
    private Double doubleField;
    private BigDecimal decimalField;
    
    private List<Integer> listField;
    private Map<String,String> mapField;
    
    public String getStringField() {
        return stringField;
    }
    public void setStringField(String stringField) {
        this.stringField = stringField;
    }
    public Boolean getBooleanField() {
        return booleanField;
    }
    public void setBooleanField(Boolean boolField) {
        this.booleanField = boolField;
    }
    public LocalDate getLocalDateField() {
        return localDateField;
    }
    public void setLocalDateField(LocalDate localDateField) {
        this.localDateField = localDateField;
    }
    public Instant getInstantField() {
        return instantField;
    }
    public void setInstantField(Instant instantField) {
        this.instantField = instantField;
    }
    public Integer getIntegerField() {
        return integerField;
    }
    public void setIntegerField(Integer intField) {
        this.integerField = intField;
    }
    public Short getShortField() {
        return shortField;
    }
    public void setShortField(Short shortField) {
        this.shortField = shortField;
    }
    public Byte getByteField() {
        return byteField;
    }
    public void setByteField(Byte byteField) {
        this.byteField = byteField;
    }
    public Long getLongField() {
        return longField;
    }
    public void setLongField(Long longField) {
        this.longField = longField;
    }
    public Float getFloatField() {
        return floatField;
    }
    public void setFloatField(Float floatField) {
        this.floatField = floatField;
    }
    public Double getDoubleField() {
        return doubleField;
    }
    public void setDoubleField(Double doubleField) {
        this.doubleField = doubleField;
    }
    public BigDecimal getDecimalField() {
        return decimalField;
    }
    public void setDecimalField(BigDecimal decimalField) {
        this.decimalField = decimalField;
    }
    public List<Integer> getListField() {
        return listField;
    }
    public void setListField(List<Integer> listField) {
        this.listField = listField;
    }
    public Map<String, String> getMapField() {
        return mapField;
    }
    public void setMapField(Map<String, String> mapField) {
        this.mapField = mapField;
    }
    
    public static AllTypesJava8DatesPOJO randomInstance() {
        AllTypesJava8DatesPOJO result = new AllTypesJava8DatesPOJO();
        result.setStringField( String.valueOf(random.nextInt(10)) );
        result.setBooleanField( random.nextBoolean() );
        
        result.setByteField( (byte) (random.nextInt() % Byte.MAX_VALUE) );
        result.setShortField( (short) (random.nextInt() % Short.MAX_VALUE) );
        result.setIntegerField( random.nextInt(Integer.MAX_VALUE) );
        result.setLongField( (long) random.nextInt());
        
        result.setFloatField(random.nextFloat());
        result.setDoubleField(random.nextDouble());
        result.setDecimalField( BigDecimal.valueOf(random.nextDouble()) );
        
        result.setLocalDateField( LocalDate.of(1970 + random.nextInt(60), 1 + random.nextInt(11), 1 + random.nextInt(27)) );
        result.setInstantField( Instant.ofEpochMilli(random.nextInt()) );
        
        //result.setListField(random.ints(10).boxed().collect(Collectors.toList()));
        //result.setMapField(Collections.singletonMap("key", String.valueOf(random.nextBoolean())));
        
        return result;
    }
}