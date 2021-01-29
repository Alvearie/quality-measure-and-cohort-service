/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.fhir.client.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Wrapper for connection properties needed to communicate with a FHIR server.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class FhirServerConfig {
	public static enum LogInfo {
		ALL, REQUEST_BODY, REQUEST_HEADERS, REQUEST_SUMMARY, RESPONSE_BODY, RESPONSE_HEADERS, RESPONSE_SUMMARY
	}

	private String endpoint;
	@JsonInclude(Include.NON_NULL)
	private String user;
	@JsonInclude(Include.NON_NULL)
	private String password;
	@JsonInclude(Include.NON_NULL)
	private String token;
	@JsonInclude(Include.NON_NULL)
	private Map<String, String> headers;
	@JsonInclude(Include.NON_NULL)
	private List<String> cookies;
	@JsonInclude(Include.NON_NULL)
	private List<LogInfo> logInfo;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	public List<LogInfo> getLogInfo() {
		return logInfo;
	}

	public void setLogInfo(List<LogInfo> logInfo) {
		this.logInfo = logInfo;
	}

	@JsonIgnore
	public Map<String, String> getAdditionalHeaders() {
		return Collections.emptyMap();
	}
}
