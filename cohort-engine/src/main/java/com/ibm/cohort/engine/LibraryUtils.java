/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.cql_annotations.r1.CqlToElmInfo;
import org.hl7.cql_annotations.r1.ObjectFactory;
import org.w3c.dom.Element;

/**
 * Helper methods for dealing with ELM Library resources.
 */
public class LibraryUtils {
	/**
	 * When a Library is deserialized via the CqlLibraryReader helper class the
	 * annotations do not get mapped to Objects. This method helps bridge the gap.
	 * 
	 * @param library
	 * @return List of Annotation objects which are usually {@link CqlToElmInfo} or
	 *         {@link CqlToElmError} objects.
	 * @throws Exception
	 */
	public static List<Object> unmarshallAnnotations(Library library) throws Exception {

		List<Object> annotations = new ArrayList<>();
		if (library.getAnnotation() != null) {
			JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller u = ctx.createUnmarshaller();

			for (Object elem : library.getAnnotation()) {
				JAXBElement<?> j = (JAXBElement<?>) u.unmarshal((Element) elem);
				annotations.add(j.getValue());
			}
		}
		return annotations;
	}
}
