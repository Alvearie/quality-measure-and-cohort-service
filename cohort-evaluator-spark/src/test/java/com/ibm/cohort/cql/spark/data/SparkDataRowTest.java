/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.MetadataBuilder;
import org.junit.Test;
import org.opencds.cqf.cql.engine.runtime.Code;

import com.ibm.cohort.cql.spark.BaseSparkTest;
import com.ibm.cohort.datarow.exception.UnsupportedConversionException;

public class SparkDataRowTest extends BaseSparkTest {
    private static final String SNOMED = "http://snomed.info/sct";
    private static final long serialVersionUID = 1L;

    @Test
    public void testConvertAllDatatypes() {
        Java8API useJava8API = Java8API.ENABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        int rowCount = 1000;
        List<AllTypesJava8DatesPOJO> data = new ArrayList<>();
        for(int i=0; i<rowCount; i++) {
            data.add( AllTypesJava8DatesPOJO.randomInstance() );
        }
        Dataset<Row> df = session.createDataFrame(data, AllTypesJava8DatesPOJO.class);
        assertEquals(rowCount, df.count());

        df.foreach( row -> {
            final SparkDataRow sdr = new SparkDataRow(typeConverter, row);
            sdr.getFieldNames().forEach( fn -> {
                sdr.getValue(fn);
            });
        });
    }
    
    @Test
    public void testConversionSemantics() {
        Java8API useJava8API = Java8API.ENABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        List<AllTypesJava8DatesPOJO> data = new ArrayList<>();

        AllTypesJava8DatesPOJO pojo = new AllTypesJava8DatesPOJO();
        pojo.setBooleanField(true);
        pojo.setStringField("test");
        pojo.setByteField((byte)1);
        pojo.setShortField((short)2);
        pojo.setIntegerField(3);
        pojo.setLongField(4L);
        pojo.setFloatField(5.5f);
        pojo.setDoubleField((double)6.6);
        pojo.setDecimalField(BigDecimal.TEN);
        pojo.setLocalDateField(LocalDate.of(2001, 06, 15));
        pojo.setInstantField(Instant.ofEpochMilli(1111000000));
        //pojo.setListField(Arrays.asList(10));
        //pojo.setMapField(Collections.singletonMap("hello", "world"));

        data.add( pojo );

        Dataset<Row> df = session.createDataFrame(data, AllTypesJava8DatesPOJO.class);
        assertEquals(1, df.count());

        df.foreach( row -> {
            SparkDataRow sdr = new SparkDataRow(typeConverter, row);
            assertTrue( sdr.getValue( "stringField" ) instanceof String );
            assertEquals( pojo.getStringField(), (String) sdr.getValue("stringField") );

            assertTrue( sdr.getValue( "booleanField" ) instanceof Boolean );
            assertEquals( pojo.getBooleanField(), (Boolean) sdr.getValue("booleanField") );

            assertTrue( sdr.getValue( "byteField" ) instanceof Integer );
            assertEquals( Integer.valueOf(1), (Integer) sdr.getValue("byteField") );

            assertTrue( sdr.getValue( "shortField" ) instanceof Integer );
            assertEquals( Integer.valueOf(2), (Integer) sdr.getValue("shortField") );

            assertTrue( sdr.getValue( "integerField" ) instanceof Integer );
            assertEquals( pojo.getIntegerField(), (Integer) sdr.getValue("integerField") );

            assertTrue( sdr.getValue( "longField" ) instanceof Integer );
            assertEquals( Integer.valueOf(4), (Integer) sdr.getValue("longField") );

            assertTrue( sdr.getValue( "floatField" ) instanceof BigDecimal );
            assertEquals( BigDecimal.valueOf(5.5f), (BigDecimal) sdr.getValue("floatField") );

            assertTrue( sdr.getValue( "doubleField" ) instanceof BigDecimal );
            assertEquals( BigDecimal.valueOf(6.6), (BigDecimal) sdr.getValue("doubleField") );

            assertTrue( sdr.getValue( "decimalField" ) instanceof BigDecimal );
            BigDecimal decimal = (BigDecimal) sdr.getValue("decimalField");
            assertEquals( pojo.getDecimalField().longValue(), decimal.longValue() );

            assertTrue( sdr.getValue( "instantField" ) instanceof org.opencds.cqf.cql.engine.runtime.DateTime );
            org.opencds.cqf.cql.engine.runtime.DateTime dt = (org.opencds.cqf.cql.engine.runtime.DateTime) sdr.getValue( "instantField" );
            assertEquals( pojo.getInstantField().atZone(ZoneId.systemDefault()).toOffsetDateTime(), dt.getDateTime() );

            assertTrue( sdr.getValue( "localDateField" ) instanceof org.opencds.cqf.cql.engine.runtime.Date );
            org.opencds.cqf.cql.engine.runtime.Date date = (org.opencds.cqf.cql.engine.runtime.Date) sdr.getValue( "localDateField" );
            assertEquals( pojo.getLocalDateField(), date.getDate() );

            //assertTrue( sdr.getValue( "listField") instanceof java.util.List );
            //assertTrue( sdr.getValue( "mapField") instanceof java.util.Map );
        });
    }
    
    @Test
    public void testConversionSemanticsNullValues() {
        Java8API useJava8API = Java8API.ENABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        List<AllTypesJava8DatesPOJO> data = new ArrayList<>();

        AllTypesJava8DatesPOJO pojo = new AllTypesJava8DatesPOJO();
        data.add( pojo );

        Dataset<Row> df = session.createDataFrame(data, AllTypesJava8DatesPOJO.class);
        assertEquals(1, df.count());

        df.foreach( row -> {
            SparkDataRow sdr = new SparkDataRow(typeConverter, row);
            assertEquals( pojo.getStringField(), sdr.getValue("stringField") );
            assertEquals( pojo.getBooleanField(), sdr.getValue("booleanField") );
            assertEquals( pojo.getByteField(), sdr.getValue("byteField") );
            assertEquals( pojo.getShortField(), sdr.getValue("shortField") );
            assertEquals( pojo.getIntegerField(), sdr.getValue("integerField") );
            assertEquals( pojo.getLongField(), sdr.getValue("longField") );
            assertEquals( pojo.getFloatField(), (BigDecimal) sdr.getValue("floatField") );
            assertEquals( pojo.getDoubleField(), (BigDecimal) sdr.getValue("doubleField") );
            assertEquals( pojo.getDecimalField(), sdr.getValue("decimalField") );
            assertEquals( pojo.getInstantField(), sdr.getValue( "instantField" ));
            assertEquals( pojo.getLocalDateField(), sdr.getValue( "localDateField" ) );
            assertEquals( pojo.getListField(), sdr.getValue( "listField" ) );
            assertEquals( pojo.getMapField(), sdr.getValue( "mapField" ) );
        });
    }
    
    @Test
    public void testDateTimeConversionSemanticsWithoutJava8Enabled() {
        Java8API useJava8API = Java8API.DISABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        List<PreJava8DateTypesPOJO> data = new ArrayList<>();

        Date expectedDate = new Date(1111000000);
        Timestamp expectedTimestamp = new Timestamp(1111000000);

        PreJava8DateTypesPOJO pojo = new PreJava8DateTypesPOJO();
        pojo.setDateField(expectedDate);
        pojo.setTimestampField(expectedTimestamp);

        data.add( pojo );

        Dataset<Row> df = session.createDataFrame(data, PreJava8DateTypesPOJO.class);
        assertEquals(1, df.count());

        df.show();

        df.foreach( row -> {
            SparkDataRow sdr = new SparkDataRow(typeConverter, row);

            assertTrue( sdr.getValue( "timestampField" ) instanceof org.opencds.cqf.cql.engine.runtime.DateTime );
            org.opencds.cqf.cql.engine.runtime.DateTime dt = (org.opencds.cqf.cql.engine.runtime.DateTime) sdr.getValue( "timestampField" );
            assertEquals( expectedTimestamp.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime(), dt.getDateTime() );

            assertTrue( sdr.getValue( "dateField" ) instanceof org.opencds.cqf.cql.engine.runtime.Date );
            org.opencds.cqf.cql.engine.runtime.Date date = (org.opencds.cqf.cql.engine.runtime.Date) sdr.getValue( "dateField" );
            assertEquals( expectedDate.toLocalDate(), date.getDate() );
        });
    }
    
    @Test
    public void testUnhandledTypeConversionSemantics() {
        Java8API useJava8API = Java8API.ENABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        List<UnhandledTypesPOJO> data = new ArrayList<>();

        int expectedFieldCount = 4;
        UnhandledTypesPOJO pojo = new UnhandledTypesPOJO();
        data.add( pojo );

        Dataset<Row> df = session.createDataFrame(data, UnhandledTypesPOJO.class);
        assertEquals(1, df.count());
        assertEquals(expectedFieldCount, df.schema().names().length);

        SparkDataRow sdr = new SparkDataRow(typeConverter, df.head());
        assertEquals(expectedFieldCount, sdr.getFieldNames().size());
        sdr.getFieldNames().forEach( fn -> {
            assertThrows(UnsupportedConversionException.class, () -> sdr.getValue(fn) );
        });
    }
    
    @Test
    public void testConversionSemanticsAsCodeAllFields() {
        Metadata codeMetadata = new MetadataBuilder()
                .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
                .putString(SparkDataRow.SYSTEM_COL, "system")
                .putString(SparkDataRow.DISPLAY_COL, "display")
                .build();

        CodeWithMetadataPOJO pojo = new CodeWithMetadataPOJO("123", SNOMED, "A Code");
        
        SparkDataRow sdr = runMetadataTest(pojo, codeMetadata);
        
        Object converted = sdr.getValue("code");
        Code code = (Code) converted;
        assertEquals( pojo.getCodeStr(), code.getCode() );
        assertEquals( pojo.getSystem(), code.getSystem() );
        assertEquals( pojo.getDisplay(), code.getDisplay() );
    }
    
    @Test
    public void testConversionSemanticsAsCodeCodeAndDefaultSystem() {
        Metadata codeMetadata = new MetadataBuilder()
                .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
                .putString(SparkDataRow.SYSTEM, SNOMED)
                .putString(SparkDataRow.SYSTEM_COL, null)
                .putString(SparkDataRow.DISPLAY_COL, null)
                .build();

        CodeWithMetadataPOJO pojo = new CodeWithMetadataPOJO("123", null, null);
        
        SparkDataRow sdr = runMetadataTest(pojo, codeMetadata);
        
        Object converted = sdr.getValue("code");
        Code code = (Code) converted;
        assertEquals( pojo.getCodeStr(), code.getCode() );
        assertEquals( SNOMED, code.getSystem() );
        assertNull( code.getDisplay() );
        assertNull( code.getVersion() );
    }
    
    @Test
    public void testConversionSemanticsAsCodeOnlyCode() {

        CodeWithMetadataPOJO pojo = new CodeWithMetadataPOJO("123", SNOMED, "A Code");

        Metadata codeMetadata = new MetadataBuilder()
                .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
                .build();

        
        SparkDataRow sdr = runMetadataTest(pojo, codeMetadata);
        
        Object converted = sdr.getValue("code");
        Code code = (Code) converted;
        assertEquals( pojo.getCodeStr(), code.getCode() );
        assertNull( code.getSystem() );
        assertNull( code.getDisplay() );
    }
    
    @Test
    public void testConversionSemanticsAsCodeFalse() {
        
        CodeWithMetadataPOJO pojo = new CodeWithMetadataPOJO("123", SNOMED, "A Code");

        Metadata codeMetadata = new MetadataBuilder()
                .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.FALSE)
                .build();

        SparkDataRow sdr = runMetadataTest(pojo, codeMetadata);

        Object converted = sdr.getValue("code");
        assertEquals( pojo.getCodeStr(), (String) converted );
    }
    
    public SparkDataRow runMetadataTest(CodeWithMetadataPOJO pojo, Metadata codeMetadata) {
        Java8API useJava8API = Java8API.ENABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        int expectedFieldCount = 4;

        List<CodeWithMetadataPOJO> data = new ArrayList<>();
        data.add(pojo);

        Dataset<Row> df = session.createDataFrame(data, CodeWithMetadataPOJO.class);
        df = df.withColumn("code", df.col("codeStr"), codeMetadata);
        assertEquals(1, df.count());
        assertEquals(expectedFieldCount, df.schema().names().length);

        return new SparkDataRow(typeConverter, df.head());
    }

    @Test
    public void testConversionSemanticsAsAnyStringSingle() {
        Java8API useJava8API = Java8API.DISABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        AnyStringSinglePOJO primaryField = new AnyStringSinglePOJO();
        primaryField.setPrimaryField("primaryString");
        primaryField.setOtherField("otherString");

        Dataset<Row> df = session.createDataFrame(Collections.singletonList(primaryField), AnyStringSinglePOJO.class);
        df = df.withColumn("primaryCol", df.col("primaryField"), createPrimaryColumnMetadata("primary_"));

        assertEquals(1, df.count());

        SparkDataRow sdr = new SparkDataRow(typeConverter, df.first());
        assertThat((List<String>) sdr.getValue("primaryCol"), containsInAnyOrder("primaryString"));
    }

    @Test
    public void testConversionSemanticsAsAnyStringMultiple() {
        Java8API useJava8API = Java8API.DISABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        AnyStringMultiplePOJO expected = new AnyStringMultiplePOJO("any1", "any2", "any3");

        Dataset<Row> df = session.createDataFrame(Collections.singletonList(expected), AnyStringMultiplePOJO.class);
        df = df.withColumn("anyPrimary", df.col("anyPrimary"), createPrimaryColumnMetadata("any"));

        assertEquals(1, df.count());

        SparkDataRow sdr = new SparkDataRow(typeConverter, df.first());
        assertThat((List<String>) sdr.getValue("anyPrimary"), containsInAnyOrder(expected.getAnyPrimary(), expected.getAny2(), expected.getAny3()));
    }

    @Test
    public void testConversionSemanticsAsAnyCodeMultiple() {
        Java8API useJava8API = Java8API.DISABLED;
        SparkSession session = initializeSession(useJava8API);

        SparkTypeConverter typeConverter = new SparkTypeConverter(useJava8API.getValue());

        AnyCodeMultiplePOJO expected = new AnyCodeMultiplePOJO();
        Code expectedCode1 = new Code().withCode("strA").withSystem("systemA").withDisplay("displayA");
        expected.setCodeStr1(expectedCode1.getCode(), expectedCode1.getSystem(), expectedCode1.getDisplay());
        Code expectedCode2 = new Code().withCode("strB").withSystem("systemB").withDisplay("displayB");
        expected.setCodeStr2(expectedCode2.getCode(), expectedCode2.getSystem(), expectedCode2.getDisplay());
        Code expectedCode3 = new Code().withCode("strC").withSystem("systemC").withDisplay("displayC");
        expected.setCodeStr3(expectedCode3.getCode(), expectedCode3.getSystem(), expectedCode3.getDisplay());
        
        Dataset<Row> df = session.createDataFrame(Collections.singletonList(expected), AnyCodeMultiplePOJO.class);
        Metadata primaryColumnMetadata = createPrimaryColumnMetadata("codeStr");
        Metadata anyPrimaryMetadata = new MetadataBuilder()
            .withMetadata(primaryColumnMetadata)
            .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
            .putString(SparkDataRow.SYSTEM_COL, "system1")
            .putString(SparkDataRow.DISPLAY_COL, "display1")
            .build();
        df = df.withColumn("codeStr1", df.col("codeStr1"), anyPrimaryMetadata);
        
        Metadata any2Metadata = new MetadataBuilder()
            .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
            .putString(SparkDataRow.SYSTEM_COL, "system2")
            .putString(SparkDataRow.DISPLAY_COL, "display2")
            .build();
        df = df.withColumn("codeStr2", df.col("codeStr2"), any2Metadata);

        Metadata any3Metadata = new MetadataBuilder()
            .putBoolean(SparkDataRow.IS_CODE_COL, Boolean.TRUE)
            .putString(SparkDataRow.SYSTEM_COL, "system3")
            .putString(SparkDataRow.DISPLAY_COL, "display3")
            .build();
        df = df.withColumn("codeStr3", df.col("codeStr3"), any3Metadata);

        assertEquals(1, df.count());

        SparkDataRow sdr = new SparkDataRow(typeConverter, df.first());
        assertThat((List<Code>) sdr.getValue("codeStr1"), containsInAnyOrder(samePropertyValuesAs(expectedCode1), samePropertyValuesAs(expectedCode2), samePropertyValuesAs(expectedCode3)));
    }
    
    private Metadata createPrimaryColumnMetadata(String anyColumnPrefix) {
        return new MetadataBuilder()
            .putBoolean(SparkDataRow.IS_PRIMARY_ANY_COL, true)
            .putString(SparkDataRow.ANY_COL_PREFIX, anyColumnPrefix)
            .build();
    }

}
