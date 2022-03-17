/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.elm.execution.IncludeDef;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.data.ExternalFunctionProvider;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;

import com.ibm.cohort.cql.data.CqlDataProvider;
import com.ibm.cohort.cql.data.CqlSystemDataProvider;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.library.CqlLibraryDeserializationException;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.CqlVersionedIdentifier;
import com.ibm.cohort.cql.library.ProviderBasedLibraryLoader;
import com.ibm.cohort.cql.terminology.CqlTerminologyProvider;

public class CqlContextFactory {

    /**
     * This class captures all of the data that is included in a CQL context
     * object that either must remain the same between use of the context
     * because it cannot be changed once set or generally does remain the
     * same between evaluations (libraryProvider, terminologyProvider).
     */
    protected static class ContextCacheKey {
        final public CqlLibraryProvider libraryProvider;
        final public CqlTerminologyProvider terminologyProvider;
        final public ExternalFunctionProvider externalFunctionProvider;
        final public CqlVersionedIdentifier topLevelLibraryIdentifier;
        final public ZonedDateTime evaluationDateTime;
        final public Map<String,Parameter> parameters;

        public ContextCacheKey(
            CqlLibraryProvider libraryProvider,
            CqlVersionedIdentifier topLevelLibraryIdentifier,
            CqlTerminologyProvider terminologyProvider,
            ExternalFunctionProvider externalFunctionProvider,
            ZonedDateTime evaluationDateTime,
            Map<String, Parameter> parameters ) {
            this.libraryProvider = libraryProvider;
            this.topLevelLibraryIdentifier = topLevelLibraryIdentifier;
            this.terminologyProvider = terminologyProvider;
            this.externalFunctionProvider = externalFunctionProvider;
            this.evaluationDateTime = evaluationDateTime;
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o2) {
            boolean isEqual = false;

            if( o2 instanceof ContextCacheKey ) {
                ContextCacheKey k2 = (ContextCacheKey) o2;

                isEqual = Objects.equals(topLevelLibraryIdentifier, k2.topLevelLibraryIdentifier) &&
                        Objects.equals( libraryProvider, k2.libraryProvider ) &&
                        Objects.equals( terminologyProvider, k2.terminologyProvider ) &&
                        Objects.equals( externalFunctionProvider, k2.externalFunctionProvider ) &&
                        Objects.equals( evaluationDateTime, k2.evaluationDateTime ) &&
                        Objects.equals( parameters, k2.parameters );

            }

            return isEqual;
        }

        @Override
        public int hashCode() {
            return Objects.hash(topLevelLibraryIdentifier, libraryProvider, terminologyProvider, externalFunctionProvider, evaluationDateTime, parameters);
        }
    }

    public static final boolean DEFAULT_CACHE_EXPRESSIONS = true;
    public static final boolean DEFAULT_CACHE_CONTEXTS = true;

    private static ConcurrentMap<ContextCacheKey, Context> CONTEXT_CACHE = new ConcurrentHashMap<>();

    /**
     * Controls whether or not the CQL engine caches the result of each expression.
     * This is a trade off of memory vs. runtime performance. The default is true
     * and this is the recommended setting for most applications.
     */
    private boolean cacheExpressions = DEFAULT_CACHE_EXPRESSIONS;
    private boolean cacheContexts = DEFAULT_CACHE_CONTEXTS;

    private ExternalFunctionProvider externalFunctionProvider;

    public CqlContextFactory() {

    }

    public boolean isCacheExpressions() {
        return cacheExpressions;
    }

    public void setCacheExpressions(boolean cacheExpressions) {
        this.cacheExpressions = cacheExpressions;
    }

    public boolean isCacheContexts() {
        return cacheContexts;
    }

    public void setCacheContexts(boolean cacheContexts) {
        this.cacheContexts = cacheContexts;
    }


    public void setExternalFunctionProvider(ExternalFunctionProvider externalFunctionProvider) {
        this.externalFunctionProvider = externalFunctionProvider;
    }

    /**
     * Initialize a CQL Engine Context object with the provided settings.
     *
     * @param libraryProvider           Provider for CQL library resources
     * @param topLevelLibraryIdentifier Identifier for the top level library
     * @param terminologyProvider       Provider for CQL terminology resources
     * @param dataProvider              Provider for data that underlies the evaluation
     * @param evaluationDateTime        Date and time that will be considered "now" during
     *                                  CQL evaluation. If null, then ZonedDateTime.now()
     *                                  will be used each time a context object is
     *                                  initialized.
     * @param contextData               Name-Value pair of context name + context value
     *                                  corresponding to the unique ID of an individual
     *                                  context that is being evaluated. In a Patient
     *                                  context, this would be the Patient ID, etc.
     * @param parameters                Optional input parameters for the CQL evaluation
     * @param debug                     Debug configuration.
     * @return initialized Context object
     * @throws CqlLibraryDeserializationException if the specified library cannot be
     *                                            loaded
     */
    public Context createContext(CqlLibraryProvider libraryProvider, CqlVersionedIdentifier topLevelLibraryIdentifier,
            CqlTerminologyProvider terminologyProvider, CqlDataProvider dataProvider, ZonedDateTime evaluationDateTime,
            Pair<String, String> contextData, Map<String, Parameter> parameters, CqlDebug debug)
            throws CqlLibraryDeserializationException {
        ContextCacheKey key = new ContextCacheKey(
                libraryProvider,
                topLevelLibraryIdentifier,
                terminologyProvider,
                this.externalFunctionProvider,
                evaluationDateTime,
                parameters
        );

        Context cqlContext;
        if (cacheContexts) {
            cqlContext = CONTEXT_CACHE.computeIfAbsent(key, this::createContext);
        }
        else {
            cqlContext = createContext(key);
        }

        // The following data elements need to be reset on every evaluation...

        Set<String> uris = getModelUrisForLibrary(cqlContext.getCurrentLibrary());
        for (String modelUri : uris) {
            cqlContext.registerDataProvider(modelUri, dataProvider);
        }

        resetContextValues(cqlContext);
        cqlContext.clearExpressions();

        if( contextData != null ) {
            cqlContext.setContextValue(contextData.getKey(), contextData.getValue());
        }

        DebugMap debugMap = createDebugMap(debug);
        cqlContext.setDebugMap(debugMap);

        cqlContext.setExpressionCaching(this.cacheExpressions);

        cqlContext.clearEvaluatedResources();

        return cqlContext;
    }

    /**
     * Initialize a CQL context from the values associated with the provided
     * CQL Context Key. This encapsulates the set of initializations that are
     * static from run to run.
     *
     * @param contextKey container for stable context settings
     * @return initialized CQL Context object
     * @throws CqlLibraryDeserializationException if the specified library cannot be loaded
     */
    protected Context createContext(ContextCacheKey contextKey) throws CqlLibraryDeserializationException {
        LibraryLoader libraryLoader = new ProviderBasedLibraryLoader(contextKey.libraryProvider);

        VersionedIdentifier vid = new VersionedIdentifier().withId(contextKey.topLevelLibraryIdentifier.getId())
                .withVersion(contextKey.topLevelLibraryIdentifier.getVersion());

        Library entryPoint = libraryLoader.load(vid);
        Context cqlContext;
        if (contextKey.evaluationDateTime != null) {
            cqlContext = new Context(entryPoint, contextKey.evaluationDateTime, new CqlSystemDataProvider());
        } else {
            cqlContext = new Context(entryPoint, new CqlSystemDataProvider());
        }

        cqlContext.registerExternalFunctionProvider(vid, this.externalFunctionProvider);
        registerExternalIncludes(cqlContext, cqlContext.getCurrentLibrary());

        cqlContext.registerLibraryLoader(libraryLoader);

        cqlContext.registerTerminologyProvider(contextKey.terminologyProvider);

        if( contextKey.parameters != null ) {
            Library library = cqlContext.getCurrentLibrary();

            for( Map.Entry<String,Parameter> entry : contextKey.parameters.entrySet() ) {
                Object cqlValue = entry.getValue().toCqlType();
                cqlContext.setParameter(library.getLocalId(), entry.getKey(), cqlValue);
            }

            if (library.getIncludes() != null && library.getIncludes().getDef() != null) {
                for (IncludeDef def : library.getIncludes().getDef()) {
                    String name = def.getLocalIdentifier();
                    for (Map.Entry<String, Parameter> parameterValue : contextKey.parameters.entrySet()) {
                        Object cqlValue = parameterValue.getValue().toCqlType();
                        cqlContext.setParameter(name, parameterValue.getKey(), cqlValue);
                    }
                }
            }
        }

        return cqlContext;
    }

    private void registerExternalIncludes(Context context, Library currentLibrary) {
        Library.Includes includes = currentLibrary.getIncludes();

        if (includes != null) {
            for (IncludeDef include : includes.getDef()) {
                VersionedIdentifier vid = new VersionedIdentifier()
                    .withId(include.getLocalIdentifier())
                    .withVersion(include.getVersion());
                context.registerExternalFunctionProvider(vid, this.externalFunctionProvider);
            }
        }
    }

    /**
     * Given the context names in the configured library, clear
     * any previously set values.
     *
     * @param cqlContext configured CQL context object
     */
    protected static void resetContextValues(Context cqlContext) {
        Set<String> contexts = getContextNames(cqlContext);

        // To be completely thorough, we would want to do the same as above to collect
        // contexts from the included libraries, but that is a potentially expensive
        // graph walk. Trusting for now that this is enough.

        contexts.stream().forEach(ctx -> cqlContext.setContextValue(ctx, Optional.empty()));
    }

    /**
     * Retrieve the set of non-null context names associated with
     * defines in the library configured with the provided context.
     *
     * @param cqlContext configured CQL context object
     * @return set of non-null context names
     */
    protected static Set<String> getContextNames(Context cqlContext) {
        return cqlContext.getCurrentLibrary().getStatements().getDef().stream().map(d -> d.getContext())
                .filter( Objects::nonNull ).collect(Collectors.toSet());
    }

    /**
     * Helper method for initializing a debug map based on the provided CqlDebug
     * enum.
     *
     * @param debug Debug configuration
     * @return DebugMap
     */
    protected DebugMap createDebugMap(CqlDebug debug) {
        DebugMap debugMap = new DebugMap();
        if( debug == CqlDebug.DEBUG ) {
            debugMap.setIsLoggingEnabled(true);
        } else if( debug == CqlDebug.TRACE ) {
            debugMap.setIsCoverageEnabled(true);
        } else {
            debugMap.setIsLoggingEnabled(false);
            debugMap.setIsCoverageEnabled(false);
        }
        return debugMap;
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
