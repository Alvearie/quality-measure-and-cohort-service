/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.ProviderBasedLibraryLoader;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;

public class CqlContextFactory {

    /**
     * Stores the timezone relative datetime that represents "now" during CQL engine
     * execution. Defaults to ZonedDateTime.now() when not provided.
     */
    private ZonedDateTime evaluationDateTime = null;

    /**
     * Controls whether CQL engine trace logging is enabled. Defaults to false.
     */
    private boolean debug = false;

    /**
     * Controls whether or not the CQL engine caches the result of each expression.
     * This is a trade off of memory vs. runtime performance. The default is true
     * and this is the recommended setting for most applications.
     */
    private boolean cacheExpressions = true;

    public CqlContextFactory() {

    }

    public boolean isDebug() {
        return debug;
    }

    public CqlContextFactory setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public ZonedDateTime getEvaluationDateTime() {
        return this.evaluationDateTime;
    }

    public CqlContextFactory setEvaluationDateTime(ZonedDateTime now) {
        this.evaluationDateTime = now;
        return this;
    }

    public Context createContext(CqlLibraryProvider libraryProvider, CqlLibraryDescriptor topLevelLibrary,
            CqlTerminologyProvider terminologyProvider, CqlDataProvider dataProvider)
            throws CqlLibraryDeserializationException {
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(libraryProvider);

        VersionedIdentifier vid = new VersionedIdentifier().withId(topLevelLibrary.getLibraryId())
                .withVersion(topLevelLibrary.getVersion());

        Library entryPoint = libraryLoader.load(vid);
        Context cqlContext = null;
        if (evaluationDateTime != null) {
            cqlContext = new Context(entryPoint, evaluationDateTime);
        } else {
            cqlContext = new Context(entryPoint);
        }

        cqlContext.registerLibraryLoader(libraryLoader);

        cqlContext.registerTerminologyProvider(terminologyProvider);

        Set<String> uris = getModelUrisForLibrary(entryPoint);
        for (String modelUri : uris) {
            cqlContext.registerDataProvider(modelUri, dataProvider);
        }

        DebugMap debugMap = new DebugMap();
        debugMap.setIsLoggingEnabled(this.debug);
        cqlContext.setDebugMap(debugMap);

        cqlContext.setExpressionCaching(this.cacheExpressions);

        // TODO - Input parameters
        // cqlContext.setParameter(null, name, value);

        return cqlContext;
    }

    /**
     * Get the set of distinct model URIs used by the provided CQL libraries.
     * 
     * @param library Library to interrogate.
     * @return set of distinct model URIs referenced by the libraries excepting the
     *         System library that is referenced by all models.
     */
    protected Set<String> getModelUrisForLibrary(Library library) {
        return library.getUsings().getDef().stream().filter(d -> !d.getLocalIdentifier().equals("System"))
                .map(d -> d.getUri()).collect(Collectors.toSet());
    }
}
