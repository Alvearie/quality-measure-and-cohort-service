/*
 *
 *  * (C) Copyright IBM Corp. 2021
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.ibm.cohort.translator.provider;

import java.io.File;

import javax.xml.bind.JAXB;

import org.cqframework.cql.cql2elm.ModelInfoProvider;
import org.hl7.elm_modelinfo.r1.ModelInfo;

//todo move
public class CustomModelInfoProvider implements ModelInfoProvider {
	public String filePath = null;

	public CustomModelInfoProvider(String filePath){
		this.filePath = filePath;
	}

	@Override
	public ModelInfo load() {
		return JAXB.unmarshal(new File(filePath), ModelInfo.class);
	}
}
