package com.ibm.cohort.engine.api.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class CohortEngineRestConstantsTest {

	/**
	 * Test CohortEngineRestConstants definitions.
	 */

	@Test
	public void testServiceConstant() throws Exception {
		assertEquals("com.ibm.cohort.engine.api.service", CohortEngineRestConstants.SERVICE_SWAGGER_PACKAGES);
		assertEquals("new_date_api_feature", CohortEngineRestConstants.DARK_LAUNCHED_NEW_DATE_API_FEATURE);
		assertEquals("new_date_parameter_feature", CohortEngineRestConstants.DARK_LAUNCHED_NEW_DATE_PARAMETER_FEATURE);
		assertEquals("v1", CohortEngineRestConstants.SERVICE_MAJOR_VERSION);
		assertEquals("IBM Cohort Engine", CohortEngineRestConstants.SERVICE_TITLE);
		assertEquals("Service to evaluate cohorts and measures", CohortEngineRestConstants.SERVICE_DESCRIPTION);
	}
}
