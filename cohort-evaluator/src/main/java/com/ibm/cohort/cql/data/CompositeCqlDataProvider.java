/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;

public class CompositeCqlDataProvider extends CompositeDataProvider implements CqlDataProvider {

    public CompositeCqlDataProvider(ModelResolver modelResolver, RetrieveProvider retrieveProvider) {
        super(modelResolver, retrieveProvider);
    }
}
