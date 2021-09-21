/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.library;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CqlLibraryDeserializationExceptionTest {
    @Test
    public void testInitializeSuccess() {
        String expectedMessage = "My message";
        Exception expectedRootCause = new IllegalArgumentException("That's wrong!");

        
        CqlLibraryDeserializationException ex;
        
        ex = new CqlLibraryDeserializationException();
        
        ex = new CqlLibraryDeserializationException(expectedMessage);
        assertEquals(expectedMessage, ex.getMessage());
        
        ex = new CqlLibraryDeserializationException(expectedRootCause);
        assertEquals(ex.getCause(), expectedRootCause);
        
        ex = new CqlLibraryDeserializationException(expectedMessage, expectedRootCause);
        assertEquals(expectedMessage, ex.getMessage());
        assertEquals(ex.getCause(), expectedRootCause);
    }
}
