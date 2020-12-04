/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.engine.api.service;

import java.util.List;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.watson.common.service.base.ServiceApplication;
import com.ibm.watson.common.service.base.ServiceBaseConstants;
import com.ibm.watson.solutions.api.listeners.SwaggerSetup;


@ApplicationPath(ServiceBaseConstants.SERVICE_API_ROOT)
public class CohortEngineRestApplication extends ServiceApplication {
	private static final Logger logger = LoggerFactory.getLogger(CohortEngineRestApplication.class.getName());

	public CohortEngineRestApplication() {
		super();

	}

	/**
	 * (non-Javadoc)
	 *
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		// Get classes from parent
		Set<Class<?>> classes = super.getClasses();

		// Add application-specific classes here
		// Note that any handler added here will have its static block run but not its constructor
		// when the first URL hits the service.
		// The handler itself is instantiated on each rest call inside that handler.
		// Expensive initialization code should be done in the handler's static block or
		// as a lazy initialize once during the handler's construction and stored in a
		// static member variable.
		classes.add( CohortEngineRestStatusHandler.class );
		classes.add( CohortEngineRestHandler.class );

		// Uncomment the following lines for chainable services that will use the ContainerGroup model.
		// You will also need to add either service-container-model-utils-v1.9 or service-container-model-utils-v2.4 to your pom file
		// Add Jackson and our own custom serializer / deserializers
		//classes.add( JacksonJsonProvider.class);
		//classes.add( ObjectMapperProvider.class );

		// Add swagger providers
		List<Class<?>> swaggerClasses = SwaggerSetup.getApplicationClassesForSwagger();
		classes.addAll(swaggerClasses);

		// Log handlers
		StringBuffer handlerList = new StringBuffer();
		for(Class<?> handler : classes) {
			handlerList.append(handler.getSimpleName()+" ");
		}
		logger.info("Handler classes: "+handlerList.toString());

		return classes;
	}

}
