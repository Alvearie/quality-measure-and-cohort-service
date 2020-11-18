/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.watson.common.service.base.ServiceBaseInitialization;

/**
 * This class is called early in the servlet startup and can be used to do
 * any initialization the service requires.  It extends the ServiceBaseInitialization class
 * which will also do any initialization that is common across all services (e.g. swagger bean
 * config).
 *
 * The extending class must first call the setter methods in the base class to pass in the
 * configuration details about the service.  The @WebListener annotation must be coded here
 * for this class to get called at servlet startup time.
 *
 *
 */
@WebListener
public class CohortEngineRestInitialization extends ServiceBaseInitialization {
	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestInitialization.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		//no-op
	}

	/**
	 * Initialize swagger via the ServletContextEvent listener
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {

		logger.info(">contextInitialized()");

		// Initialize base class configuration
		setServiceTitle(CohortEngineRestConstants.SERVICE_TITLE);
		setServiceDescription(CohortEngineRestConstants.SERVICE_DESCRIPTION);
		setSwaggerPackages(CohortEngineRestConstants.SERVICE_SWAGGER_PACKAGES);

		setBuildVersion(ServiceBuildConstants.VERSION);
		setBuildTime(ServiceBuildConstants.TIMESTAMP);

		super.contextInitialized(event);

		//
		// Service-specific initialization
		//


		logger.info("<contextInitialized()");
	}

}