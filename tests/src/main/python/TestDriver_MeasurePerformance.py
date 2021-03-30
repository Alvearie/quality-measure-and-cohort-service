
import os
import json
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
        for testName in testValues:
            tests.append((testValues[testName]['jsonMeasureConfigurationFile'], testValues[testName]['targets'], testName))
    return tests

class Test(object):

    # parameterize controls how tests are bucketed in the Taurus output
    @pytest.mark.parametrize("jsonMeasureConfigurationFile, targets, testName", setup())
    def test(self, jsonMeasureConfigurationFile, targets, testName):
        self.execute(jsonMeasureConfigurationFile, targets)

    # Execute submits a query and waits for the subprocess to complete
    def execute(self, jsonMeasureConfigurationFile, targets):
        callDetails = ["java", "-Xms1G", "-Xmx1G", "-Djavax.net.ssl.trustStore="+os.environ["TRUSTSTORE"], "-Djavax.net.ssl.trustStorePassword="+os.environ["TRUSTSTORE_PASSWORD"], "-Djavax.net.ssl.trustStoreType="+os.environ["TRUSTSTORE_TYPE"], "-classpath", jar, "com.ibm.cohort.cli.MeasureCLI"]
        if os.getenv('DATA_FHIR_SERVER_DETAILS'):
            callDetails.append("-d")
            callDetails.append(os.environ['DATA_FHIR_SERVER_DETAILS'])
        if os.getenv('TERM_FHIR_SERVER_DETAILS'):
            callDetails.append("-t")
            callDetails.append(os.environ['TERM_FHIR_SERVER_DETAILS'])
        if os.getenv('MEASURE_FHIR_SERVER_DETAILS'):
            callDetails.append("-m")
            callDetails.append(os.environ['MEASURE_FHIR_SERVER_DETAILS'])
        if jsonMeasureConfigurationFile:
            callDetails.append("-j")
            callDetails.append(jsonMeasureConfigurationFile)
        for val in targets:
            callDetails.append("-c")
            callDetails.append(val)
        print("callDetails: " + " ".join(callDetails))
        process = subprocess.Popen(callDetails, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

        decodedOutput = set()
        for line in process.stdout:
            print(line)
            decodedOutput.add(line.decode('utf-8').rstrip("\n"))

        for expectedOutput in self.createExpectedOutput(targets):
            assert expectedOutput in decodedOutput, 'Output did not contain: ' + expectedOutput

    def createExpectedOutput(self, targets):
        returnValue = []
        for target in targets:
            returnValue.append("Evaluating: " + target)
        return returnValue
