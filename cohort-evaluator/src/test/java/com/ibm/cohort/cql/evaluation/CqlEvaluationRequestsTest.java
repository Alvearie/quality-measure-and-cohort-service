package com.ibm.cohort.cql.evaluation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibraryDescriptor;

public class CqlEvaluationRequestsTest {
    @Test
    public void testSetGet() {
        Map<String,Object> globalParameters = new HashMap<>();
        globalParameters.put("String", "Hello,World");
        globalParameters.put("Integer", 10);
        
        Map<String,Object> localParameters = new HashMap<>();
        localParameters.put("String", "Goodbye, Cruel World");
        localParameters.put("Float", 1.29f);
        
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
}
