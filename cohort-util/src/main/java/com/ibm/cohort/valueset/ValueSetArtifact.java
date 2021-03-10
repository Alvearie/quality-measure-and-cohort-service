/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.valueset;


import org.hl7.fhir.r4.model.MetadataResource;

public class ValueSetArtifact {
	private MetadataResource resource;
	private String url;
	private String name;
	private String id;

	public MetadataResource getResource() {
		return resource;
	}

	public void setResource(MetadataResource resource) {
		this.resource = resource;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ValueSetArtifact that = (ValueSetArtifact) o;

		if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		return id != null ? id.equals(that.id) : that.id == null;
	}

	@Override
	public int hashCode() {
		int result = resource != null ? resource.hashCode() : 0;
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}
}
