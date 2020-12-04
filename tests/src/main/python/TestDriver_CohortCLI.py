
import unittest
import os
import time
import csv
import json
import re
import subprocess
import pytest

currentDir=os.getcwd()
baseDir = currentDir + '/'
libraries=os.environ['LIBRARY_PATH']
testFile=baseDir + os.environ['TESTS_JSON']
jar = os.environ['JAR']

def setup():
    os.chdir(baseDir)
    tests = list()
    with open(testFile) as f:
        data = json.load(f)
        testValues = data['tests']
        for testValue in testValues.values():
            tests.append((testValue['params'], testValue['library'], testValue['version'], testValue['targets'], testValue['response'], testValue['source'], testValue['expressions'], testValue['measureServer']))
    return tests
 
class Test(object):

    @pytest.mark.parametrize("params, library, version, targets,output,source,expressions,measureServer",setup())
    def test1(self, params, library, version, targets, output, source, expressions, measureServer):
        self.execute(params, library, version, targets, output, source, expressions, measureServer)

    # Execute submits a query and validates the return.
    def execute(self, params, library, version,  targets, output, source, expressions, measureServer):
        output = re.sub('@\w+', '@',output)
        o = output.split('\n')
        callDetails = ["java", "-Xms1G", "-Xmx1G", "-Djavax.net.ssl.trustStore="+os.environ["TRUSTSTORE"], "-Djavax.net.ssl.trustStorePassword="+os.environ["TRUSTSTORE_PASSWORD"], "-Djavax.net.ssl.trustStoreType="+os.environ["TRUSTSTORE_TYPE"], "-jar", jar, "-f", libraries, "-l", library]
        if os.environ['DATA_FHIR_SERVER_DETAILS']:
            callDetails.append("-d")
            callDetails.append(os.environ['DATA_FHIR_SERVER_DETAILS'])
        if os.environ['TERM_FHIR_SERVER_DETAILS']:
            callDetails.append("-t")
            callDetails.append(os.environ['TERM_FHIR_SERVER_DETAILS'])
        if params:
            for val in params:
                callDetails.append("-p") 
                callDetails.append(val)
        if version:
            callDetails.append("-v")
            callDetails.append(version)
        if source:
            callDetails.append("-s")
            callDetails.append(source)
        if expressions:
            for val in expressions:
               callDetails.append("-e")
               callDetails.append(val)
        if measureServer:
            callDetails.append("-m")
            callDetails.append(measureServer)
        for val in targets:
            callDetails.append("-c")
            callDetails.append(val)
        print("callDetails: " + " ".join(callDetails))
        out = subprocess.Popen(callDetails, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        tmpout=""
        for line in out.stdout:
            temp=line.decode('utf-8')
            if "Expression: " in temp or "Context: " in temp:
                tmpout=tmpout+temp
        print("Output: \n" + tmpout)
        out = re.sub('@\w+', '@',tmpout)
        out = re.sub('_history/\w+', '_history', out)
        print("Output: \n" + out)
        respOut = out.splitlines()
        error = "\n"
        for line in o:
            assert line in respOut, 'Did not contain: ' + line + '\nContained: ' + error.join(respOut)
        
        print("In respOut:")
        for line in respOut:
            assert line in o, 'Did not contain: ' + line + '\nContained: ' + error.join(o)

    def setup(self):
        time.sleep(2) # Seems like some service somewhere needs a few seconds to recover between tests.