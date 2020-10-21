/*
 * (C) Copyright IBM Copr. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate additional configuration properties specific to an 
 * IBM FHIR Server, specifically the multitenancy approach.
 */
public class IBMFhirServerConfig extends FhirServerConfig {
	/**
	 * IBM FHIR Server uses HTTP headers to control which underlying tenant contains
	 * the data being retrieved. This is the default header name used in the base
	 * configuration files, but it can be changed by the user as needed for their
	 * execution environment.
	 */
	public static final String DEFAULT_TENANT_ID_HEADER = "X-FHIR-TENANT-ID";
	
	private String tenantId;
	private String tenantIdHeader = DEFAULT_TENANT_ID_HEADER;
	
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantIdHeader() {
		return tenantIdHeader;
	}

	public void setTenantIdHeader(String tenantIdHeader) {
		this.tenantIdHeader = tenantIdHeader;
	}
	
	@Override
	public Map<String,String> getAdditionalHeaders() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put( this.tenantIdHeader, this.tenantId );
		return headers;
	}
}
