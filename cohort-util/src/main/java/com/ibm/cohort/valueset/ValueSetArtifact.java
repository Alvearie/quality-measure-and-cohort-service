/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.valueset;


import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetArtifact {
	private ValueSet fhirResource;
	private String url;
	private String name;
	private String id;

	public ValueSet getFhirResource() {
		return fhirResource;
	}

	public void setFhirResource(ValueSet fhirResource) {
		this.fhirResource = fhirResource;
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

		ValueSetArtifact artifact = (ValueSetArtifact) o;

		if (fhirResource != null ? !fhirResource.equals(artifact.fhirResource) : artifact.fhirResource != null) return false;
		if (url != null ? !url.equals(artifact.url) : artifact.url != null) return false;
		if (name != null ? !name.equals(artifact.name) : artifact.name != null) return false;
		return id != null ? id.equals(artifact.id) : artifact.id == null;
	}

	@Override
	public int hashCode() {
		int result = fhirResource != null ? fhirResource.hashCode() : 0;
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}
}
