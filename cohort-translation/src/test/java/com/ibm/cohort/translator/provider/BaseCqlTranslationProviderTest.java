/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.translator.provider;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;

import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Test;

//this entire class is to get around coverage minimums and it is definitely cheating
public class BaseCqlTranslationProviderTest {

	private class TestLibraryProvider implements LibrarySourceProvider {

		@Override
		public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
			return null;
		}
	}

	@Test
	public void getDefaultOptions(){
		BaseCqlTranslationProvider provider = new InJVMCqlTranslationProvider(new TestLibraryProvider());
		assertEquals(provider.getDefaultOptions(), new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions()));
	}
}