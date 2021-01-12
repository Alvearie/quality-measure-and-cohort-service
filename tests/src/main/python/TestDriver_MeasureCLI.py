
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
testFile=baseDir + os.environ['TESTS_JSON']
jar = os.environ['JAR']

def setup():
    os.chdir(baseDir)
    tests = list()
    with open(testFile) as f:
        data = json.load(f)
        testValues = data['tests']
        for testValue in testValues.values():
            regEx=False
            try:
                regEx=testValue['regEx']
            except:
                regEx=False
            tests.append((testValue['jsonMeasureConfigurationFile'], testValue['resource'], testValue['params'], testValue['targets'], testValue['response'], testValue['measureServer'], regEx))
    return tests
 
class Test(object):

    @pytest.mark.parametrize("jsonMeasureConfigurationFile, resource, params, targets, output, measureServer, regEx", setup())
    def test1(self, jsonMeasureConfigurationFile, resource, params, targets, output, measureServer, regEx):
        self.execute(jsonMeasureConfigurationFile, resource, params, targets, output, measureServer, regEx)

    # Execute submits a query and validates the return.
    def execute(self, jsonMeasureConfigurationFile, resource, params, targets, output, measureServer, regEx):
        o = output.split('\n')
        callDetails = ["java", "-Xms1G", "-Xmx1G", "-Djavax.net.ssl.trustStore="+os.environ["TRUSTSTORE"], "-Djavax.net.ssl.trustStorePassword="+os.environ["TRUSTSTORE_PASSWORD"], "-Djavax.net.ssl.trustStoreType="+os.environ["TRUSTSTORE_TYPE"], "-classpath", jar, "com.ibm.cohort.cli.MeasureCLI"]
        if os.environ['DATA_FHIR_SERVER_DETAILS']:
            callDetails.append("-d")
            callDetails.append(os.environ['DATA_FHIR_SERVER_DETAILS'])
        if os.environ['TERM_FHIR_SERVER_DETAILS']:
            callDetails.append("-t")
            callDetails.append(os.environ['TERM_FHIR_SERVER_DETAILS'])
        if jsonMeasureConfigurationFile:
            callDetails.append("-j")
            callDetails.append(jsonMeasureConfigurationFile)
        if resource:
            callDetails.append("-r")
            callDetails.append(resource)
        if params:
            for val in params:
                callDetails.append("-p")
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
            if not "[main]" in temp:
                tmpout=tmpout+temp
        out=tmpout
        if regEx:
            for line in o:
                assert re.match(line, out), 'Did not contain: ' + line + '\nContained: ' + out
        else:
            respOut = out.splitlines()
            error = "\n"
            for line in o:
                assert line in respOut, 'Did not contain: ' + line + '\nContained: ' + error.join(respOut)
        
            print("In respOut:")
            for line in respOut:
                assert line in o, 'Did not contain: ' + line + '\nContained: ' + error.join(o)
