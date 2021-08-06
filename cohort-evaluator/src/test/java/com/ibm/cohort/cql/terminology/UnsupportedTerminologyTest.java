package com.ibm.cohort.cql.terminology;

import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

public class UnsupportedTerminologyTest {
    CqlTerminologyProvider terminologyProvider;
    
    @Before
    public void setUp() {
        terminologyProvider = new UnsupportedTerminologyProvider();
    }
    
    @Test
    public void testLookupUnsupported() {
        assertThrows( UnsupportedOperationException.class, () -> terminologyProvider.lookup(null,null) );
    }
    
    @Test
    public void testInUnsupported() {
        assertThrows( UnsupportedOperationException.class, () -> terminologyProvider.in(null,null) );
    }
    
    @Test
    public void testExpandUnsupported() {
        assertThrows( UnsupportedOperationException.class, () -> terminologyProvider.expand(null) );
    }
}
