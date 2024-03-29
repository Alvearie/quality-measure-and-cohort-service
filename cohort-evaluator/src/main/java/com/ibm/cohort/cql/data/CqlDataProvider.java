/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import java.util.function.Supplier;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.PHIObfuscator;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.RedactingPHIObfuscator;

public interface CqlDataProvider extends DataProvider {

    @Override
    default Supplier<PHIObfuscator> phiObfuscationSupplier() {
        return RedactingPHIObfuscator::new;
    }
}
