/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cohort.cql.spark.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cohort.cql.evaluation.CqlEvaluationRequest;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;

public class CqlEvaluationRequestUtil {
    private static Map<CqlEvaluationRequest,String> requestToParamsJson = new ConcurrentHashMap<>();
    
    public static void clearCache() {
        requestToParamsJson.clear();
    }
    
    public static String getKeyParametersColumnData(CqlEvaluationRequest request, Set<String> keyParameterNames) {
        return requestToParamsJson.computeIfAbsent(request, req -> {
            String parametersJson = "{}";
            
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
            return parametersJson;
        });
    }
}
