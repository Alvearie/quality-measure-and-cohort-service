/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.datarow.engine;

import org.opencds.cqf.cql.engine.data.CompositeDataProvider;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.datarow.model.DataRow;

public class DataRowDataProvider extends CompositeDataProvider implements CqlDataProvider {

    public DataRowDataProvider(Class<? extends DataRow> dataRowImpl, DataRowRetrieveProvider retrieveProvider) {
        super(new DataRowModelResolver(dataRowImpl), retrieveProvider);
    }
}
