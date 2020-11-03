/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ca.uhn.fhir.jpa.api.dao;

/**
 * Provide a placeholder for the HAPI DaoRegistry so that we can exclude the
 * real one and all of its many dependencies from the classpath. This gets
 * loaded by the cqf-ruler MeasureEvaluation class, but is never used for our
 * use case at execution time, so it is safe to remove.
 */
public class DaoRegistry {

	public boolean isResourceTypeSupported(String theResourceType) {
		throw new UnsupportedOperationException(
				"You reached an execution point that should not occur. This implementation of the HAPI DaoRegistry exists only to satisfy the ClassLoader. The real implementation was excluded from the classpath to eliminate a large number of unnecessary dependencies.");
	}

}
