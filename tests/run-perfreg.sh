#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
# Helper to run bzt on the pod specifying a test yaml file to be executed
runTest() {
  echo "Running test" ${1}
  kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} && bzt ${1}"
}

# Replace jar, server details, and filename in scenarios yaml files
populateTaurusYaml() {
  yamlfile=$1
  
  sed -i \
  -e "/JAR/s|:.*$|: \"${SHADED_JAR}\"|" \
  -e "/MEASURE_FHIR_SERVER_DETAILS/s|:.*$|: \"${KNOWLEDGE_TENANT}\"|" \
  -e "/DATA_FHIR_SERVER_DETAILS/s|:.*$|: \"${TESTFVT_TENANT}\"|" ${yamlfile}
}

. tests/setupEnvironmentVariables.sh

POD_NAME=engine-cohort-perfreg-test

bash tests/create-taurus-pod.sh ${POD_NAME}

populateTaurusYaml ${SCENARIOS_DIR}/performance/performanceRegression.yaml
kubectl -n ${CLUSTER_NAMESPACE} cp ${SCENARIOS_DIR}/performance/performanceRegression.yaml ${POD_NAME}:/bzt-configs/tests/scenarios/performance/

runTest "/bzt-configs/tests/scenarios/performance/performanceRegression.yaml"

PERF_REG_RESULTS="${OUTPUT_DIR}/perf-reg-results"
echo "Copying test results xml files to ${PERF_REG_RESULTS}"
mkdir -p ${PERF_REG_RESULTS}
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results" "${PERF_REG_RESULTS}"

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
mv combinedResults.xml perfreg.xml