/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.cqframework.cql.elm.execution.Library;

public class OptimizedCqlLibraryReader {
	private static JAXBContext context;
	private static Unmarshaller unmarshaller;

	private OptimizedCqlLibraryReader() {
	}

	public static synchronized Unmarshaller getUnmarshaller() throws JAXBException {
		if (context == null) {
			context = JAXBContext.newInstance(OptimizedObjectFactory.class);
		}

		if (unmarshaller == null) {
			unmarshaller = context.createUnmarshaller();
		}

		return unmarshaller;
	}

	@SuppressWarnings("unchecked")
	public static synchronized Library read(String xml) throws JAXBException {
		Object result = getUnmarshaller().unmarshal(new StringReader(xml));

		return ((JAXBElement<Library>)result).getValue();
	}

    @SuppressWarnings("unchecked")
    public static synchronized Library read(InputStream stream) throws JAXBException {
        Object result = getUnmarshaller().unmarshal(new StreamSource(stream));

        return ((JAXBElement<Library>)result).getValue();
    }
}
