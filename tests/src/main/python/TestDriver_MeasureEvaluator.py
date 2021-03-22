
import unittest
import os
import timeit
import functools
import pytest
import javabridge
import time
import csv
import json
import re

num=int(os.environ['PERF_EXECUTION_COUNT']) # number of times to execute a query to generate an average.
engineWrapper='' # Used to hold reference to the engine once it has been fired up.
currentDir=os.getcwd()
baseDir = currentDir + '/'
testLibrary = os.environ['TEST_LIBRARY']
testFile=baseDir + os.environ['TESTS_JSON']

 
class Test(object):
    def setup():
        os.chdir(baseDir)
        # This will eventually need to read these in from a file.
        tests = list()
        with open(testFile) as f:
            data = json.load(f)
            testValues = data['tests']
            for testValue in testValues.values():
                tests.append((testValue['library'], testValue['target'], testValue['response'], float(testValue['avg'])))
        return tests

    @pytest.mark.parametrize("library,target,output,avg",setup())
    def test1(self,library,target,output,avg):
        self.runner(library,target,output,avg)

    # Runner will call execute in a loop to perform benchmark validation.
    def runner(self, library, target, output, avg):
        
        t = timeit.Timer(functools.partial(self.execute, library, target, output))
        val = t.timeit(num) # This is the total execute time to run execute num times.
        avgRec = val/float(num) # Since num can change want to compare against expected avg execution.
        assert avgRec < avg, 'Value was: %f' % avgRec

    # Execute submits a query and validates the return.
    def execute(self, library, target, output):
        output = re.sub('@\w+', '@',output)
        o = output.split('\n')
        out = engineWrapper.execute(library, target)
        out = re.sub('@\w+', '@',out)
        respOut = out.splitlines()
        error = "\n"
        for line in o:
            assert line in respOut, 'Did not contain: ' + line + '\nContained: ' + error.join(respOut)
        
        print("In respOut:")
        for line in respOut:
            assert line in o, 'Did not contain: ' + line + '\nContained: ' + error.join(o)

    def setup_class(self):
        global engineWrapper
        testWrapper = baseDir+os.environ['TEST_WRAPPER']
        cohortEngine = baseDir+os.environ['COHORT_ENGINE']
        javabridge.start_vm(args=["-Xms512M", "-Xmx1G", "-Djavax.net.ssl.trustStore="+os.environ['TRUSTSTORE'], "-Djavax.net.ssl.trustStorePassword="+os.environ['TRUSTSTORE_PASSWORD'], "-Djavax.net.ssl.trustStoreType="+os.environ['TRUSTSTORE_TYPE']], run_headless=True, class_path=javabridge.JARS + [cohortEngine, testWrapper]) # Start the JVM with modified classpath.
        engineWrapper = javabridge.JClassWrapper("com.ibm.cohort.engine.test.TestWrapper")() # Get an instance of the test wrapper.
        engineWrapper.warm(baseDir+os.environ['DATA_FHIR_SERVER_DETAILS'],baseDir+os.environ['TERM_FHIR_SERVER_DETAILS'],libraries, testLibrary, "d6587935-b9e4-188d-7e9f-a8dc909f4216") # Warm up the JV and submit a noise query.

    def teardown_class(self):
        javabridge.kill_vm() # The JVM must die.
    
    def setup(self):
        time.sleep(2) # Seems like some service somewhere needs a few seconds to recover between tests.