#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
runTest() {
  echo "Running test" ${1}
  kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} && bzt ${1}"
}

# Replace jar, server details, and filename in scenarios yaml files
populateTaurusYaml() {
  yamlfile=$1
  
  sed -i \
  -e "/JAR/s|:.*$|: \"${SHADED_JAR}\"|" \
  -e "/MEASURE_FHIR_SERVER_DETAILS/s|:.*$|: \"${DEFAULT_TENANT}\"|" \
  -e "/DATA_FHIR_SERVER_DETAILS/s|:.*$|: \"${TESTFVT_TENANT}\"|" ${yamlfile}
}

checkForTestResults() {
  resultsDirectory=$1
  resultsFile=$2
  sleepTime=$3
  remainingRetries=$4

  localFileToCheck="${resultsDirectory}/${resultsFile}"

  kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results/${resultsFile}" "${localFileToCheck}"

  while [[ ! -f "${localFileToCheck}" ]] && [[ remainingRetries -gt 0 ]];
  do
    echo "${localFileToCheck} not found. Attempting to retrieve from test pod."
    kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results/${resultsFile}" "${localFileToCheck}"
    remainingRetries=$((remainingRetries-1))
    echo "Sleeping for ${sleepTime} seconds and retrying...${remainingRetries} retries remaining."
    sleep ${sleepTime}
  done
}

. tests/setupEnvironmentVariables.sh

POD_NAME=engine-cohort-ct-perf-test

bash tests/create-taurus-pod.sh ${POD_NAME}

populateTaurusYaml ${SCENARIOS_DIR}/performance/performanceCTScenarios.yaml
kubectl -n ${CLUSTER_NAMESPACE} cp ${SCENARIOS_DIR}/performance/performanceCTScenarios.yaml ${POD_NAME}:/bzt-configs/tests/scenarios/performance/

runTest "/bzt-configs/tests/scenarios/performance/performanceCTScenarios.yaml"

PERF_REG_RESULTS="${OUTPUT_DIR}/ct-perf-results"
mkdir -p ${PERF_REG_RESULTS}
checkForTestResults "${PERF_REG_RESULTS}" "performanceCTTests.xml" 60 30

# Make sure results are displayed to stdout even if the previous exec command returns early
kubectl exec -it ${POD_NAME} -n ${CLUSTER_NAMESPACE} -- bash -c "cat /tmp/artifacts/*.ldjson"

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

$pythonBinary ${TEST_DIR}/scripts/xmlCombiner.py ${PERF_REG_RESULTS}/*.xml
mv combinedResults.xml performancetests.xml