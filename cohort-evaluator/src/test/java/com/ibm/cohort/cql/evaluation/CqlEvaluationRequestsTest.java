/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ibm.cohort.cql.evaluation.parameters.DecimalParameter;
import com.ibm.cohort.cql.evaluation.parameters.IntegerParameter;
import com.ibm.cohort.cql.evaluation.parameters.Parameter;
import com.ibm.cohort.cql.evaluation.parameters.StringParameter;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequestsTest {
    @Test
    public void testSetGet() {
        Map<String,Parameter> globalParameters = new HashMap<>();
        globalParameters.put("String", new StringParameter("Hello,World"));
        globalParameters.put("Integer", new IntegerParameter(10));
        
        Map<String,Parameter> localParameters = new HashMap<>();
        localParameters.put("String", new StringParameter("Goodbye, Cruel World"));
        localParameters.put("Float", new DecimalParameter("1.29f"));
        
        CqlLibraryDescriptor desc = new CqlLibraryDescriptor().setLibraryId("SampleLibrary").setVersion("1.0.0");
        
        CqlEvaluationRequest r1 = new CqlEvaluationRequest();
        r1.setDescriptor(desc);
        r1.setParameters(localParameters);
        r1.setExpressions(Collections.singleton("IsFemale"));
        r1.setContextKey("Patient");
        r1.setContextValue("NA");

        assertEquals( desc, r1.getDescriptor() );
        assertEquals( localParameters, r1.getParameters() );
        assertEquals( Collections.singleton("IsFemale"), r1.getExpressions() );
        assertEquals( "Patient", r1.getContextKey() );
        assertEquals( "NA", r1.getContextValue() );
        
        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setGlobalParameters(globalParameters);
        requests.setEvaluations(Arrays.asList(r1));
        
        assertEquals( globalParameters, requests.getGlobalParameters() );
        assertEquals( 1, requests.getEvaluations().size() );
    }
    
    @Test
    public void testGetEvaluationsForContextNoneFound() {
        CqlEvaluationRequest request1 = new CqlEvaluationRequest();
        request1.setContextKey("contextXYZ");
        CqlEvaluationRequest request2 = new CqlEvaluationRequest();
        
        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setEvaluations(Arrays.asList(request1, request2));
        
        assertTrue(requests.getEvaluationsForContext("context1").isEmpty());
    }

    @Test
    public void testGetEvaluationsForContextContextsFound() {
        CqlEvaluationRequest request1 = new CqlEvaluationRequest();
        request1.setContextKey("context1");
        CqlEvaluationRequest request2 = new CqlEvaluationRequest();
        request2.setContextKey("context1");
        request2.setContextValue("abcd");
        CqlEvaluationRequest request3 = new CqlEvaluationRequest();
        request3.setContextKey("contextXYZ");


        CqlEvaluationRequests requests = new CqlEvaluationRequests();
        requests.setEvaluations(Arrays.asList(request1, request2));

        List<CqlEvaluationRequest> actualRequests = requests.getEvaluationsForContext("context1");

        assertEquals(2, actualRequests.size());
        assertTrue(actualRequests.contains(request1));
        assertTrue(actualRequests.contains(request2));
    }
}
