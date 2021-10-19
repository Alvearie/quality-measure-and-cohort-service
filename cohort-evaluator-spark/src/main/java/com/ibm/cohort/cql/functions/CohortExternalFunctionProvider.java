package com.ibm.cohort.cql.functions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.opencds.cqf.cql.engine.data.SystemExternalFunctionProvider;

/**
 * Wrapper around SystemExternalFunctionProvider that implements equals and hashCode
 * so that it can be cached.
 *
 */
public class CohortExternalFunctionProvider extends SystemExternalFunctionProvider {

    private final List<Method> functions;

    public CohortExternalFunctionProvider(List<Method> staticFunctions) {
        super(staticFunctions);
        functions = staticFunctions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortExternalFunctionProvider that = (CohortExternalFunctionProvider) o;
        return Objects.equals(functions, that.functions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functions);
    }
}
