#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

#set hard coded Pod name to make it simpler to find
POD_NAME="cohort-engine-test-pod"
echo "Running in pipeline, using pod name: ${POD_NAME}"

TEST_DIR=`pwd`
CONFIG_DIR=`pwd`/src/main/resources/config
LIB_DIR=`pwd`/src/main/resources/libraries
SCENARIOS_DIR=`pwd`/scenarios
OUTPUT_DIR=output

# Set up Environment variables to use later in this script where the test scenario files need to be updated to reflect the current test 
# environment. 
DEFAULT_TENANT="tests\/src\/main\/resources\/config\/fhirconfig-default-tenant.json"
TESTFVT_TENANT="tests\/src\/main\/resources\/config\/fhirconfig-testfvt-tenant.json"

BREAST_XMLFILE="\/bzt-configs\/tests\/results\/breastCancerTestScenarios.xml"
COLORECTAL_XMLFILE="\/bzt-configs\/tests\/results\/coloRectalCancerTestScenarios.xml"
LUNG_XMLFILE="\/bzt-configs\/tests\/results\/lungCancerTestScenarios.xml"
MEASURECLI_XMLFILE="\/bzt-configs\/tests\/results\/measureCLITests.xml"

# check if FHIR_CLUSTER_NAMESPACE has been set in the toolchain
# properties and use it if it is there, assume fhir will be
# running in the same namespace
if [[ -z "${FHIR_CLUSTER_NAMESPACE}" ]]
then
  FHIR_CLUSTER_NAMESPACE_VAL=${CLUSTER_NAMESPACE}
else
  FHIR_CLUSTER_NAMESPACE_VAL=${FHIR_CLUSTER_NAMESPACE}
fi

FHIR_ENDPOINT="https:\/\/fhir-internal.${FHIR_CLUSTER_NAMESPACE_VAL}.svc:9443\/fhir-server\/api\/v4"
SHADED_JAR="\/bzt-configs\/tests\/src\/main\/resources\/libraries\/cohort-cli-shaded.jar"

# Usage statement on how this script is invoked from run-fvttests.sh script
usage() {
  echo "Usage: run-taurus-pod.sh [-o OUTPUT-FILE] testfile1, testfile2 ..."
  echo "    -o OUTPUT-FILE    Final output XML file"
  echo "    -t TEST-FILES     Comma separated list of test files"
}

# Helper to run bzt on the pod specifying a test yaml file to be executed
runTest() {
  echo "Running test" ${1}
  kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} && bzt ${1}"
}

# Parse args passed in by run-fvttests.sh script when calling this script with the -o and -t options. 
# See usage statement above for details

if [[ $# -lt 4 ]] # Requires at least -o <val> -t <val>
then
  usage
fi
while getopts "o:t:" arg; do 
  case ${arg} in 
    o)
      echo "Final output ${OPTARG}"
      outputXML=${OPTARG}
      ;;
    t)
      echo "Test files ${OPTARG}"
      testFiles=(`echo ${OPTARG} | tr ',' ' '`) # Convert into array
      ;;
    ?)
      echo "Invalid option"
      usage
      exit -1
      ;;
  esac
done

# Clean up from previous runs -- Delete the pod that was spun off to run the tests on the prior run
# Also remove the output directory which was created to hold the results locally accessible inside of the cd pipeline
rm -rf ${OUTPUT_DIR}
kubectl -n ${CLUSTER_NAMESPACE} delete pod/${POD_NAME}

# Get the deployment file ready for the pod that will be spun off.
cp run-engine-taurus-deploy.yaml run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/CLUSTER_NAMESPACE_PLACEHOLDER/${CLUSTER_NAMESPACE}/g" run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/POD_NAME_PLACEHOLDER/${POD_NAME}/g" run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/TRUSTSTORE_PLACEHOLDER/${TRUSTSTORE}/" run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/TRUSTSTORE_TYPE_PLACEHOLDER/${TRUSTSTORE_TYPE}/" run-engine-taurus-deploy-with-replaced-values.yaml

cd ${TEST_DIR}

# Get the cohort-cli shaded jar from the cohort-services pod.
COHORTSVC_POD=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}" | grep Running | cut -d " " -f 1 | head -n1)
kubectl -n ${CLUSTER_NAMESPACE} cp ${COHORTSVC_POD}:/opt/alvearie/cohortEngine/cohort-cli-shaded.jar ${LIB_DIR}/cohort-cli-shaded.jar
echo "Copied cohort-cli shaded jar from the running cohort-services pod."

# Before tests can be executed prepare the test yaml files to have the right values replaced in them  

cd ${CONFIG_DIR}
# Generate fhir config file for test-fvt tenant
cp local-ibm-fhir.json fhirconfig-testfvt-tenant.json
sed -i '/\"password\"/s/:.*$/: \"'"${FHIR_USER_PASS}"'\",/' fhirconfig-testfvt-tenant.json
sed -i '/\"endpoint\"/s/:.*$/: \"'"${FHIR_ENDPOINT}"'\",/' fhirconfig-testfvt-tenant.json

#generate updated default tenant fhir config json
cp local-ibm-fhir.json fhirconfig-default-tenant.json
sed -i '/\"password\"/s/:.*$/: \"'"${FHIR_USER_PASS}"'\",/' fhirconfig-default-tenant.json
sed -i '/\"tenantId\"/s/:.*$/: \"default\",/' fhirconfig-default-tenant.json
sed -i '/\"endpoint\"/s/:.*$/: \"'"${FHIR_ENDPOINT}"'\",/' fhirconfig-default-tenant.json

cd ${SCENARIOS_DIR}
# update breastCancerTestScenarios yaml file for FHIR details, path to the cohort-cli shaded jar and the final xml file to be generated.
sed -i '/JAR/s/:.*$/: '"${SHADED_JAR}"'/' breastCancerTestScenarios.yaml
sed -i '/DATA_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' breastCancerTestScenarios.yaml
sed -i '/TERM_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' breastCancerTestScenarios.yaml
sed -i '/filename/s/:.*$/: '"${BREAST_XMLFILE}"'/' breastCancerTestScenarios.yaml

# update coloRectalCancerTestScenarios yaml file for FHIR details, path to the cohort-cli shaded jar and the final xml file to be generated.
sed -i '/JAR/s/:.*$/: '"${SHADED_JAR}"'/' coloRectalCancerTestScenarios.yaml
sed -i '/DATA_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' coloRectalCancerTestScenarios.yaml
sed -i '/TERM_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' coloRectalCancerTestScenarios.yaml
sed -i '/filename/s/:.*$/: '"${COLORECTAL_XMLFILE}"'/' coloRectalCancerTestScenarios.yaml

# update lungCancerTestScenarios yaml file for FHIR details, path to the cohort-cli shaded jar and the final xml file to be generated.
sed -i '/JAR/s/:.*$/: '"${SHADED_JAR}"'/' lungCancerTestScenarios.yaml
sed -i '/DATA_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' lungCancerTestScenarios.yaml
sed -i '/TERM_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' lungCancerTestScenarios.yaml
sed -i '/filename/s/:.*$/: '"${LUNG_XMLFILE}"'/' lungCancerTestScenarios.yaml

# update measureCLIExample-separate-measure-server.yaml for FHIR details, path to the cohort-cli shaded jar and the final xml file to be generated.
sed -i '/JAR/s/:.*$/: '"${SHADED_JAR}"'/' measureCLIExample-separate-measure-server.yaml
sed -i '/DATA_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' measureCLIExample-separate-measure-server.yaml
sed -i '/TERM_FHIR_SERVER_DETAILS/s/:.*$/: '"${TESTFVT_TENANT}"'/' measureCLIExample-separate-measure-server.yaml
sed -i '/filename/s/:.*$/: '"${MEASURECLI_XMLFILE}"'/' measureCLIExample-separate-measure-server.yaml

cd ${TEST_DIR}
# update the measureCLIExample-separate-measure-server.json file to set measureserver reference to the fhir config json for the default tenant.
sed -i 's/\"cohort-cli\/config\/local-ibm-fhir.json\"/\"'"${DEFAULT_TENANT}"'\"/g' src/main/resources/measureCLIExample-separate-measure-server.json

# Spin off the pod in which the taurus image will be executed
kubectl apply -f run-engine-taurus-deploy-with-replaced-values.yaml

sleep 15  # Sleep for 15 seconds before checking if deployed pod is running
podStatus=$(kubectl get pods -n "${CLUSTER_NAMESPACE}" --field-selector=status.phase=Running | grep ${POD_NAME})
if [ -z "${podStatus}" ]
then
  echo "Pod is not running as expected. Exiting ..."
  exit -1
fi

# Copy the whole test folder including sub-folders to the bzt-configs mount point in the mounted volume in the pod.
echo "Copying test files to the pod"
kubectl -n ${CLUSTER_NAMESPACE} cp ${TEST_DIR} ${POD_NAME}:/bzt-configs

# Before executing the tests generate the pkcs12 type trustStore with an imported SSL certificate from the FHIR Server
echo "Making createTrustStore.sh script executable inside of the pod environment."
kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- chmod +x /bzt-configs/tests/createTrustStore.sh

echo "Generating trustStore.pkcs12 object"  
kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} TRUSTSTORE=${TRUSTSTORE} CLUSTER_NAMESPACE=${CLUSTER_NAMESPACE} FHIR_CLUSTER_NAMESPACE=${FHIR_CLUSTER_NAMESPACE}&& /bzt-configs/tests/createTrustStore.sh"

# Loop through all the tests specified to be executed on the call to this script from the run-fvttests.sh script and execute bzt passing in the 
# test yaml file as a parameter. 

echo "Running all passed in tests"
for file in "${testFiles[@]}"
do
  runTest "/bzt-configs/${file}"
done

# Save test results and logs to local directories
cd ${TEST_DIR}
echo "Copying test results xml files to ${OUTPUT_DIR}/Results"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/bzt-configs/tests/results" "${OUTPUT_DIR}/Results"
echo "Copying test results log files and screenshots to ${OUTPUT_DIR}/artifacts"
kubectl -n ${CLUSTER_NAMESPACE} cp "${POD_NAME}:/tmp/artifacts" "${OUTPUT_DIR}/artifacts"

# In the sections below the final results xml files generated from executing individual tests need to be combined into a single xml file called 
# fvttest.xml for consumption by the cd pipeline to upload the the DevOps Insights console. Note: The final fvttest.xml needs to be stored 
# one level above the tests directory which in the toolchain environment is /workspace/cohort-services/cohort directory.

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
cd ${OUTPUT_DIR}/Results
$pythonBinary ${TEST_DIR}/scripts/xmlCombiner.py *.xml

cd ${TEST_DIR}
# Append the contents of the combined results xml file to the output XML file (fvttest.xml)
echo "Generating final combined results xml file ${outputXML} for DevOps Insights Console to display"
cat ${OUTPUT_DIR}/Results/combinedResults.xml >> ../${outputXML}


# At this point we are done. Clean up any temporary resources created to run the tests
echo "Cleaning up resources"
rm run-engine-taurus-deploy-with-replaced-values*

#########################################################################################################################################
# Note: We DO NOT remove delete the pod here.  We let it run for 8 hours so that there is enough time to extract out any tests related 
#       log files, captured screenshots etc. for debug purposes. The Pod is deleted right before the next run of the tests. 
#########################################################################################################################################

