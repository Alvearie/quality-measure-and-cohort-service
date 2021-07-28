/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.spark.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class PreJava8DateTypesPOJO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Date dateField;
	private Timestamp timestampField;
	
	public Date getDateField() {
		return dateField;
	}
	public void setDateField(Date dateField) {
		this.dateField = dateField;
	}
	public Timestamp getTimestampField() {
		return timestampField;
	}
	public void setTimestampField(Timestamp timestampField) {
		this.timestampField = timestampField;
	}
}
