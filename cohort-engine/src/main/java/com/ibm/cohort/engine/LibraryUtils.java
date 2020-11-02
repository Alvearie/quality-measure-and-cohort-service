/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.ParameterDef;
import org.hl7.cql_annotations.r1.CqlToElmError;
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
	
	/**
	 * Check to see if the ELM Library contains any translation
	 * error annotations.
	 * 
	 * @param library Deserialized ELM library
	 * @return true if the library contains errors. Otherwise, false.
	 */
	public static boolean hasErrors(Library library) throws Exception {
		List<Object> annotations = unmarshallAnnotations(library);
		return hasErrors(annotations);
	}
	
	/**
	 * Check to see if the list of Deserialized annotation objects
	 * contains any errors.
	 * 
	 * @param annotations List of deserialized ELM library annotations.
	 * @return true if the library contains errors. Otherwise, false.
	 */
	public static boolean hasErrors(List<Object> annotations) throws Exception {
		return annotations.stream().filter( a -> a instanceof CqlToElmError ).count() > 0;
	}

	/**
	 * Helper method that checks for translation errors in the loaded library and
	 * throws an Exception when library errors are found.
	 * 
	 * @param library ELM library to check for translation errors.
	 */
	public static void requireNoTranslationErrors(Library library) {
		try {
			if (LibraryUtils.hasErrors(library)) {
				throw new IllegalArgumentException("ELM library contains translation errors");
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to unmarshall ELM library annotations", ex);
		}
	}
	
	/**
	 * Interrogate the CQL Library for parameters with no default values and throw
	 * and exception when the parameters collection is missing at least one of those
	 * values.
	 * 
	 * @param library    CQL Library
	 * @param parameters parameter values, expected to include all non-default
	 *                   parameters in the provided library
	 */
	public static void requireValuesForNonDefaultParameters(Library library, Map<String, Object> parameters) {
		List<String> missingParameters = new ArrayList<>();

		if (library.getParameters() != null) {
			for (ParameterDef pd : library.getParameters().getDef()) {
				if (pd.getDefault() == null) {
					if (parameters == null || !parameters.containsKey(pd.getName())) {
						missingParameters.add(pd.getName());
					}
				}
			}
		}

		if (!missingParameters.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Missing parameter values for one or more non-default library parameters {}",
							missingParameters.toString()));
		}
	}
}
