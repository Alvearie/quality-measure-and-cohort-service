#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

# Helper to run bzt on the pod specifying a test yaml file to be executed
runTest() {
  echo "Running test" ${1}
  kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} && bzt ${1}"
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

. tests/setupEnvironmentVariables.sh

POD_NAME=engine-cohort-fvt-test

bash tests/create-taurus-pod.sh ${POD_NAME}

BREAST_XMLFILE="/bzt-configs/tests/results/breastCancerTestScenarios.xml"
COLORECTAL_XMLFILE="/bzt-configs/tests/results/coloRectalCancerTestScenarios.xml"
LUNG_XMLFILE="/bzt-configs/tests/results/lungCancerTestScenarios.xml"
MEASURECLI_XMLFILE="/bzt-configs/tests/results/measureCLITests.xml"


# Update yaml scenarios specific to FVT testing
# breastCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/breastCancerTestScenarios.yaml ${BREAST_XMLFILE}

# coloRectalCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/coloRectalCancerTestScenarios.yaml ${COLORECTAL_XMLFILE}

# lungCancerTestScenarios
populateTaurusYaml ${SCENARIOS_DIR}/lungCancerTestScenarios.yaml ${LUNG_XMLFILE}

# measureCLIExample-separate-measure-server.yaml and accompanying json file
populateTaurusYaml ${SCENARIOS_DIR}/measureCLIExample-separate-measure-server.yaml ${MEASURECLI_XMLFILE}
sed -i "s|\"cohort-cli/config/local-ibm-fhir.json\"|\"${DEFAULT_TENANT}\"|g" ${TEST_DIR}/src/main/resources/measureCLIExample-separate-measure-server.json

# Copy scenarios into the pod
echo "Copying fvt-specific test files to the pod"
kubectl -n ${CLUSTER_NAMESPACE} cp ${SCENARIOS_DIR} ${POD_NAME}:/bzt-configs/tests/
kubectl -n ${CLUSTER_NAMESPACE} cp ${TEST_DIR}/src/main/resources/measureCLIExample-separate-measure-server.json ${POD_NAME}:/bzt-configs/tests/src/main/resources/


# Run scenarios for fvt tests
runTest "/bzt-configs/tests/scenarios/breastCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/coloRectalCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/lungCancerTestScenarios.yaml"
runTest "/bzt-configs/tests/scenarios/measureCLIExample-separate-measure-server.yaml"


# Save test results and logs to local directories. Combine results into a single xml file
# called fvttest.xml (filename expected by the toolchain)
echo "Copying test results xml files to ${OUTPUT_DIR}/Results"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results" "${OUTPUT_DIR}/Results"
echo "Copying test results log files and screenshots to ${OUTPUT_DIR}/artifacts"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/tmp/artifacts" "${OUTPUT_DIR}/artifacts"

# First Check to see if python3 is available for use with xmlCombiner.py script. If not first install python3.8 using yum. 
pythonVersion=$(python3 --version 2>/dev/null)
pythonBinary=python3
if [ -z "${pythonVersion}" ]
then
  echo "Python check output: '${pythonVersion}'"
  echo "Missing python3, installing..."
  sudo yum -y install python38
  which python3.8
  python3.8 --version
  pythonBinary=python3.8
else 
  echo "Found 'python3' binary"
fi

# Run the xmlCombiner.py script against all XML output copied to output/Results directory earlier.
echo "Combining all XML output into single xml file called fvttest.xml"
$pythonBinary ${TEST_DIR}/scripts/xmlCombiner.py ${OUTPUT_DIR}/Results/*.xml

# Append the contents of the combined results xml file to the output XML file (fvttest.xml)
echo "Generating final combined results xml file fvttest.xml for DevOps Insights Console to display"
mv combinedResults.xml fvttest.xml
