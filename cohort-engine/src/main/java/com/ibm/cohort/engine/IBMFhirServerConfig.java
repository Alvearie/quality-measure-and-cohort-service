/*
 * (C) Copyright IBM Corp. 2020, 2020
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
	
	/**
	 * IBM FHIR Server uses HTTP headers to control which underlying datasource contains
	 * the data being retrieved. This is the default header name used in the base
	 * configuration files, but it can be changed by the user as needed for their
	 * execution environment.
	 */
	public static final String DEFAULT_DATASOURCE_ID_HEADER = "X-FHIR-DSID";
	
	private String tenantId;
	private String tenantIdHeader = DEFAULT_TENANT_ID_HEADER;
	
	private String dataSourceId;
	private String dataSourceIdHeader = DEFAULT_DATASOURCE_ID_HEADER;
	
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
	

	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public String getDataSourceIdHeader() {
		return dataSourceIdHeader;
	}

	public void setDataSourceIdHeader(String dataSourceIdHeader) {
		this.dataSourceIdHeader = dataSourceIdHeader;
	}
	
	@Override
	public Map<String,String> getAdditionalHeaders() {
		Map<String,String> headers = new HashMap<String,String>();
		if( this.tenantId != null ) {
			headers.put( this.tenantIdHeader, this.tenantId );
		}
		if( this.dataSourceId != null ) {
			headers.put( this.dataSourceIdHeader, this.dataSourceId );
		}
		return headers;
	}
}
