/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnsupportedConversionExceptionTest {
	@Test
	public void testVariousConstructors() {
		UnsupportedConversionException ex;
		
		ex = new UnsupportedConversionException();
		assertEquals(null, ex.getMessage());
		
		String expectedMessage = "Message";
		ex = new UnsupportedConversionException(expectedMessage);
		assertEquals(expectedMessage, ex.getMessage());
		
		Throwable expectedCause = new IllegalArgumentException("illegal");
		ex = new UnsupportedConversionException(expectedMessage, expectedCause);
		assertEquals(expectedMessage, ex.getMessage());
		assertEquals(expectedCause, ex.getCause());
		
		ex = new UnsupportedConversionException(expectedMessage, expectedCause, true, true);
		assertEquals(expectedMessage, ex.getMessage());
		assertEquals(expectedCause, ex.getCause());
	
		ex = new UnsupportedConversionException(expectedCause);
		assertEquals(expectedCause, ex.getCause());
	}
}
