package com.ibm.cohort.datarow.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.cql.engine.exception.InvalidCast;

import com.ibm.cohort.datarow.model.DataRow;
import com.ibm.cohort.datarow.model.SimpleDataRow;

public class DataRowModelResolverTest {

	private DataRowModelResolver resolver;
	
	@Before
	public void setUp() {
		resolver = new DataRowModelResolver();
	}
	
	@Test
	public void testDataRowPackageIsPackageName() {
		assertEquals( DataRow.class.getPackage().getName(), resolver.getPackageName() );
	}

	@Test
	public void testCreateInstanceIsUnsupported() {
		assertThrows( UnsupportedOperationException.class, () -> resolver.createInstance("anything") );
	}
	
	@Test
	public void testSetValueIsUnsupported() {
		BigDecimal decimal = new BigDecimal(10);
		assertThrows( UnsupportedOperationException.class, () -> resolver.setValue(decimal, "scale", 10) );
	}
	
	@Test
	public void testAnyDataTypeIsDataRow() {
		Random random = new Random();
		for( int i=0; i<10; i++ ) { 
			Class<?> clazz = resolver.resolveType( String.valueOf(random.nextInt()) );
			assertEquals( DataRow.class, clazz );
		}
	}
	
	@Test
	public void testGetContextPathIsAlwaysNull() {
		Random random = new Random();
		for( int i=0; i<10; i++ ) { 
			Object path = resolver.getContextPath( String.valueOf(random.nextInt()), String.valueOf(random.nextInt()) );
			assertNull( path );
		}
	}
	
	@Test
	public void testResolvePathForMissingFieldIsNull() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertNull(resolver.resolvePath(row, String.valueOf("does-not-exist") ));
	}
	
	@Test
	public void testRowEqualsRow() {
		Map<String,Object> expectations = new HashMap<>();
		expectations.put("field1", "Hello");
		
		DataRow left = new SimpleDataRow(expectations);
		DataRow right = new SimpleDataRow(expectations);
		
		assertTrue(resolver.objectEqual(left, right));
		assertTrue(resolver.objectEquivalent(left, right));
	}
	
	@Test
	public void testSetGetPackageName() {
		String expected = "new.package";
		resolver.setPackageName(expected);
		assertEquals( expected, resolver.getPackageName() );	
	}
	
	@Test
	public void testResolveTypeIsType() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertEquals( row.getClass(), resolver.resolveType(row) );	
	}
	
	@Test
	public void testIsTypeType() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertTrue( resolver.is(row, DataRow.class) );	
	}
	
	@Test
	public void testIsTypeNull() {
		DataRow row = null;
		assertNull( resolver.is(row, DataRow.class) );	
	}
	
	@Test
	public void testObjectAsTypeStrict() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertTrue( resolver.as(row, DataRow.class, true) instanceof DataRow );	
	}
	
	@Test
	public void testInvalidObjectAsTypeNonStrict() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertNull( resolver.as(row, String.class, false) );	
	}
	
	@Test
	public void testInvalidObjectAsTypeStrict() {
		DataRow row = new SimpleDataRow(Collections.emptyMap());
		assertThrows( InvalidCast.class, () -> resolver.as(row, String.class, true) );	
	}
	
	@Test
	public void testObjectAsTypeNull() {
		assertNull( resolver.as(null, DataRow.class, true) );	
	}
}
