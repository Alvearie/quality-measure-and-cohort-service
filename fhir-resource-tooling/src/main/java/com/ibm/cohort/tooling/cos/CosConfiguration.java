/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.tooling.cos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class CosConfiguration {
	@JsonInclude(Include.NON_NULL)
	private String access_key_id;
	@JsonInclude(Include.NON_NULL)
	private String secret_access_key;
	@JsonInclude(Include.NON_NULL)
	private String cos_endpoint;
	@JsonInclude(Include.NON_NULL)
	private String cos_location;

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

	public String getCos_endpoint() {
		return cos_endpoint;
	}

	public void setCos_endpoint(String cos_endpoint) {
		this.cos_endpoint = cos_endpoint;
	}

	public String getCos_location() {
		return cos_location;
	}

	public void setCos_location(String cos_location) {
		this.cos_location = cos_location;
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
