#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

# Helper to run bzt on the pod specifying a test yaml file to be executed
runTest() {
  echo "Running test" ${1}
  kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} REST_SERVER_ENDPOINT=${REST_SERVER_ENDPOINT} && bzt ${1}"
}

# Replace jar, server details, and filename in scenarios yaml files
populateTaurusYaml() {
  yamlfile=$1
  xmlfile=$2
  
  sed -i "/JAR/s|:.*$|: \"${SHADED_JAR}\"|" ${yamlfile}
  sed -i "/DATA_FHIR_SERVER_DETAILS/s|:.*$|: \"${TESTFVT_TENANT}\"|" ${yamlfile}
  sed -i "/TERM_FHIR_SERVER_DETAILS/s|:.*$|: \"${TESTFVT_TENANT}\"|" ${yamlfile}
  sed -i "/filename/s|:.*$|: \"${xmlfile}\"|" ${yamlfile}
}

# Replace fhir server connection details json in yaml file for REST API endpoint tests to use the configuration file with default tenantId
populateRestApiTestYaml() {
  yamlfile=$1
  xmlfile=$2

  sed -i "/FHIR_SERVER_DETAILS_JSON/s|:.*$|: \"${KNOWLEDGE_TENANT}\"|" ${yamlfile}
  sed -i "/filename/s|:.*$|: \"${xmlfile}\"|" ${yamlfile}
}
. tests/setupEnvironmentVariables.sh

POD_NAME=engine-cohort-fvt-test

# Call setup-config-files-for-api-tests.sh script to generate required config files for REST API tests prior to calling create_taurus-pod.sh so that these files
# get copied into the pod along with other test files.
bash tests/setup-config-files-for-api-tests.sh 
bash tests/create-taurus-pod.sh ${POD_NAME}

BREAST_XMLFILE="/bzt-configs/tests/results/breastCancerTestScenarios.xml"
COLORECTAL_XMLFILE="/bzt-configs/tests/results/coloRectalCancerTestScenarios.xml"
LUNG_XMLFILE="/bzt-configs/tests/results/lungCancerTestScenarios.xml"
MEASURECLI_XMLFILE="/bzt-configs/tests/results/measureCLITests.xml"

# XML files for REST API Endpoint tests
GET_PARAMETERS_BY_MEASURE_ID_XMLFILE="/bzt-configs/tests/results/getMeasureParametersByMeasureIdAPITests.xml"
GET_PARAMETERS_BY_MEASURE_IDENTIFIER_XMLFILE="/bzt-configs/tests/results/getMeasureParametersByIdentifierAPITests.xml"
VALUE_SET_IMPORT_XMLFILE="/bzt-configs/tests/results/valueSetImportAPITests.xml"

# Update yaml scenarios specific to FVT testing
# breastCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/breastCancerTestScenarios.yaml ${BREAST_XMLFILE}

# coloRectalCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/coloRectalCancerTestScenarios.yaml ${COLORECTAL_XMLFILE}

# lungCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/lungCancerTestScenarios.yaml ${LUNG_XMLFILE}

# measureCLIExample-separate-measure-server.yaml and accompanying json file
populateTaurusYaml ${SCENARIOS_DIR}/measureCLIExample-separate-measure-server.yaml ${MEASURECLI_XMLFILE}
sed -i "s|\"cohort-cli/config/local-ibm-fhir.json\"|\"${KNOWLEDGE_TENANT}\"|g" ${TEST_DIR}/src/main/resources/measureCLIExample-separate-measure-server.json

# Update yaml files for REST API test scenarios 
populateRestApiTestYaml ${SCENARIOS_DIR}/rest/getMeasureParametersByMeasureIdAPITests.yaml ${GET_PARAMETERS_BY_MEASURE_ID_XMLFILE}
populateRestApiTestYaml ${SCENARIOS_DIR}/rest/getMeasureParametersByIdentifierAPITests.yaml ${GET_PARAMETERS_BY_MEASURE_IDENTIFIER_XMLFILE}
populateRestApiTestYaml ${SCENARIOS_DIR}/rest/valueSetImportAPITests.yaml ${VALUE_SET_IMPORT_XMLFILE}

# Copy scenarios into the pod
echo "Copying fvt-specific test files to the pod"
kubectl -n ${CLUSTER_NAMESPACE} cp ${SCENARIOS_DIR} ${POD_NAME}:/bzt-configs/tests/
kubectl -n ${CLUSTER_NAMESPACE} cp ${TEST_DIR}/src/main/resources/measureCLIExample-separate-measure-server.json ${POD_NAME}:/bzt-configs/tests/src/main/resources/

# Run scenarios for fvt tests
runTest "/bzt-configs/tests/scenarios/breastCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/coloRectalCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/lungCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/measureCLIExample-separate-measure-server.yaml"

# REST API Endpoint Tests 
runTest "/bzt-configs/tests/scenarios/rest/getMeasureParametersByMeasureIdAPITests.yaml"
runTest "/bzt-configs/tests/scenarios/rest/getMeasureParametersByIdentifierAPITests.yaml"
runTest "/bzt-configs/tests/scenarios/rest/valueSetImportAPITests.yaml"
runTest "/bzt-configs/tests/scenarios/rest/measureEvaluationAPITests.yaml"
runTest "/bzt-configs/tests/scenarios/rest/cohortEvaluationAPITests.yaml"
runTest "/bzt-configs/tests/scenarios/rest/measureEvaluationUsingPatientListAPITests.yaml"

# Save test results and logs to local directories. Combine results into a single xml file
# called fvttest.xml (filename expected by the toolchain)
echo "Copying test results xml files to ${OUTPUT_DIR}/Results"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results" "${OUTPUT_DIR}/Results"
echo "Copying test results log files and screenshots to ${OUTPUT_DIR}/artifacts"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/tmp/artifacts" "${OUTPUT_DIR}/artifacts"

# First Check to see if python3 is available for use with xmlCombiner.py script. If not first install python3.8 using yum.
pythonBinary=python3
pythonVersion=$(${pythonBinary} --version 2>/dev/null)
if [ -z "${pythonVersion}" ]
then
  echo "Python check output: '${pythonVersion}'"
  echo "Missing ${pythonBinary}, installing..."
  sudo yum -y install python38
  which python3.8
  python3.8 --version
  pythonBinary=python3.8
else 
  echo "Found '${pythonBinary}' binary"
fi

# Run the xmlCombiner.py script against all XML output copied to output/Results directory earlier.
echo "Combining all XML output into single xml file called fvttest.xml"
$pythonBinary ${TEST_DIR}/scripts/xmlCombiner.py ${OUTPUT_DIR}/Results/*.xml

# Append the contents of the combined results xml file to the output XML file (fvttest.xml)
echo "Generating final combined results xml file fvttest.xml for DevOps Insights Console to display"
mv combinedResults.xml fvttest.xml