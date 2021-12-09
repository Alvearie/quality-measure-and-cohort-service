package com.ibm.cohort.cql.spark;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.beust.jcommander.JCommander;

public class SparkCqlEvaluatorArgsTest {
    @Test
    public void testCommaSeparatedListParameterValues() {
        String [] args = new String[] {
                "-d", "context-definitions.json",
                "-j", "cql-jobs.json",
                "-c", "cql",
                "-m", "model-info.xml,other-model.xml",
                "-i=data/input1.parquet",
                "-o=output/output1.parquet",
                "--metadata-output-path", "output/",
                "--expressions", "A,B",
                "--key-parameters", "EndDate,MinimumAge,Gender",
                "--aggregation-contexts", "Patient,Claim"
        };
        
        runTest(args);
    }
    
    @Test
    public void testMultiplyDefinedListParameterValues() {
        String [] args = new String[] {
                "-d", "context-definitions.json",
                "-j", "cql-jobs.json",
                "-c", "cql",
                "-m", "model-info.xml",
                "-m", "other-model.xml",
                "-i=data/input1.parquet",
                "-o=output/output1.parquet",
                "--metadata-output-path", "output/",
                "--expressions", "A",
                "--expressions", "B",
                "--key-parameters", "EndDate",
                "--key-parameters", "MinimumAge",
                "--key-parameters", "Gender",
                "--aggregation-contexts", "Patient",
                "--aggregation-contexts", "Claim"
        };
        
        runTest(args);
    }

    public void runTest(String[] args) {
        SparkCqlEvaluatorArgs programArgs = new SparkCqlEvaluatorArgs();
        
        JCommander commander = JCommander.newBuilder()
                .programName("SparkCqlEvaluatorArgsTest")
                .addObject(programArgs)
                .build();
        commander.parse(args);
        
        assertEquals( Arrays.asList("EndDate", "MinimumAge","Gender")
                , programArgs.keyParameterNames );
        
        assertEquals( Arrays.asList("A", "B"), programArgs.expressions );
        
        assertEquals( Arrays.asList("model-info.xml","other-model.xml")
                , programArgs.modelInfoPaths );
        
        assertEquals( Arrays.asList("Patient","Claim")
                , programArgs.aggregationContexts );
    }
}
