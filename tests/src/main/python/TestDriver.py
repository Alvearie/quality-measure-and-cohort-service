
import unittest
import os
import timeit
import functools
import pytest
import javabridge
import time
import csv

num=int(os.environ['PERF_EXECUTION_COUNT']) # number of times to execute a query to generate an average.
engineWrapper='' # Used to hold reference to the engine once it has been fired up.
currentDir=os.getcwd()
#baseDir = currentDir+'/../../'
baseDir = currentDir+'/'
libraries=os.environ['LIBRARY_PATH']
testFile=os.environ['TESTS_CSV']

def setup():
    os.chdir(baseDir)
    # This will eventually need to read these in from a file.
    tests = list()
    reader = csv.DictReader(open(baseDir+testFile))
    for row in reader:
        tests.append((row['Library'],row['Output'],float(row['Avg'])))
    return tests
    #return [('Test', 'Expression: Over the hill, Context: 1235008, Result: true',1.0), ('Test', 'Expression: Male, Context: 1235008, Result: true',0.5), ('Test', 'Expression: Female, Context: 1235008, Result: false',0.9),('Test','Random Stuff that doesnt exist.',1.0)]

class Test(object):

    @pytest.mark.parametrize("library,output,avg",setup())
    def test1(self,library,output,avg):
        self.runner(library,output,avg)

    # Runner will call execute in a loop to perform benchmark validation.
    def runner(self, library, output, avg):
        
        t = timeit.Timer(functools.partial(self.execute, library, output))
        val = t.timeit(num) # This is the total execute time to run execute num times.
        avgRec = val/float(num) # Since num can change want to compare against expected avg execution.
        assert avgRec < avg, 'Value was: %f' % avgRec

    # Execute submits a query and validates the return.
    def execute(self, library, output):
        out = engineWrapper.execute(library, "1235008") 
        assert output in out, 'Did not contain ' + output + '\nContained: ' + out

    def setup_class(self):
        global engineWrapper
        testWrapper = baseDir+os.environ['TEST_WRAPPER']
        cohortEngine = baseDir+os.environ['COHORT_ENGINE']
        javabridge.start_vm(run_headless=True, class_path=javabridge.JARS + [cohortEngine, testWrapper]) # Start the JVM with modified classpath.
        engineWrapper = javabridge.JClassWrapper("com.ibm.cohort.engine.test.TestWrapper")() # Get an instance of the test wrapper.
        engineWrapper.warm(baseDir+os.environ['DATA_FHIR_SERVER_DETAILS'],baseDir+os.environ['TERM_FHIR_SERVER_DETAILS'],libraries, "Test", "1235008") # Warm up the JV and submit a noise query.

    def teardown_class(self):
        javabridge.kill_vm() # The JVM must die.
    
    def setup(self):
        time.sleep(2) # Seems like some service somewhere needs a few seconds to recover between tests.