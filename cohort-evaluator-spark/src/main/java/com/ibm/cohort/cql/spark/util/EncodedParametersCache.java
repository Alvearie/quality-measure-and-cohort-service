/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

/**
 * Store a cache of JSON-encoded parameter data for each CqlEvaluationRequest
 * that is included in a job. The contents of the cache can be manipulated
 * using the rowGroupingDisabled and keyParametersNames fields. When 
 * rowGroupingDisabled is true, then the result is always an empty JSON
 * object. When rowGroupingDisabled is false, then the result is the JSON
 * representation of a Map of parameter values. The Map will contain all
 * parameters in the request by default or can be filtered to a subset of 
 * parameters if keyParameterNames is specified.
 * 
 * The cache data is transient and will be recalculated each time this object
 * is serialized and deserialized. In the Spark world, this effectively means
 * that the cache is reinitialized on a per-executor basis.
 */
public class EncodedParametersCache implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean rowGroupingDisabled = true;
    private Collection<String> keyParameterNames = null;
    private transient Map<CqlEvaluationRequest,String> requestToParamsJson = new ConcurrentHashMap<>();
    
    public EncodedParametersCache() {
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { 
        // needs to be reinitialized because it is transient
        requestToParamsJson = new ConcurrentHashMap<>();
        in.defaultReadObject();
    }
    
    public EncodedParametersCache setRowGroupingDisabled(boolean isEnabled) {
        this.rowGroupingDisabled = isEnabled;
        return this;
    }
    
    public EncodedParametersCache setKeyParameterNames(Collection<String> keyParameterNames) {
        this.keyParameterNames = keyParameterNames;
        return this;
    }
    
    public void clearCache() {
        requestToParamsJson.clear();
    }
    
    public String getKeyParametersColumnData(CqlEvaluationRequest request) {
        return requestToParamsJson.computeIfAbsent(request, req -> {
            String parametersJson = "{}";
            
            if( ! rowGroupingDisabled ) {
                Map<String,Parameter> keyParameters = req.getParameters();
                if( keyParameters != null ) {
                    if( keyParameterNames != null ) {
                        keyParameters = keyParameters.entrySet().stream()
                                .filter( e -> keyParameterNames.contains(e.getKey()) )
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    }
                    try {
                        parametersJson = new ObjectMapper().writeValueAsString(keyParameters);
                    } catch( Throwable th ) {
                        throw new RuntimeException("Failed to serialize parameters", th);
                    }
                }
            }
            
            return parametersJson;
        });
    }
}
