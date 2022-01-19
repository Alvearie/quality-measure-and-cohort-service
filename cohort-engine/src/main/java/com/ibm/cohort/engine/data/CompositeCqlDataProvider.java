/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.engine.data;

import java.util.function.Supplier;

import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.PHIObfuscator;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.RedactingPHIObfuscator;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;

public class CompositeCqlDataProvider extends CompositeDataProvider {

    public CompositeCqlDataProvider(ModelResolver modelResolver, RetrieveProvider retrieveProvider) {
        super(modelResolver, retrieveProvider);
    }

    @Override
    public Supplier<PHIObfuscator> phiObfuscationSupplier() {
        return RedactingPHIObfuscator::new;
    }
}
