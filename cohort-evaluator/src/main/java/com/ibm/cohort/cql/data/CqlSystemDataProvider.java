/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.data;

import java.util.function.Supplier;

import org.opencds.cqf.cql.engine.data.SystemDataProvider;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.PHIObfuscator;
import org.opencds.cqf.cql.engine.elm.execution.obfuscate.RedactingPHIObfuscator;

public class CqlSystemDataProvider extends SystemDataProvider {
    @Override
    public Supplier<PHIObfuscator> phiObfuscationSupplier() {
        return RedactingPHIObfuscator::new;
    }
}
