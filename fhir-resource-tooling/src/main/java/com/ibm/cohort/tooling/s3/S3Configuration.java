/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.tooling.s3;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class S3Configuration {
	@JsonInclude(Include.NON_NULL)
	private String access_key_id;
	@JsonInclude(Include.NON_NULL)
	private String secret_access_key;
	@JsonInclude(Include.NON_NULL)
	private String endpoint;
	@JsonInclude(Include.NON_NULL)
	private String location;

	public String getAccess_key_id() {
		return access_key_id;
	}

	public void setAccess_key_id(String access_key_id) {
		this.access_key_id = access_key_id;
	}

	public String getSecret_access_key() {
		return secret_access_key;
	}

	public void setSecret_access_key(String secret_access_key) {
		this.secret_access_key = secret_access_key;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
