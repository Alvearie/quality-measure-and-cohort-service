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

######################################### START of section specific to SPARK based fvt tests ######################################################

#Give a name to the new pod that will be spun off to be used to execute spark-submit jobs for HI FVT testing.
SPARK_POD_NAME=engine-spark-fvt-test

# Call a script "create-sparkfvt-pod.sh" to prepare and then spin off the new pod to execute spark-submit job commands
bash tests/create-sparkfvt-pod.sh ${SPARK_POD_NAME}

# Clean up any Spark Driver pod whose name starts with "cohortfvt-spark" that is hanging around from a previous spark-submit run before doing a new spark-submit.
DVRNAMEPREFIX="cohortfvt-spark"
olddriverpod=`kubectl -n ${CLUSTER_NAMESPACE} get pods --no-headers -o custom-columns=":metadata.name" | grep ${DVRNAMEPREFIX} | grep driver`
kubectl -n ${CLUSTER_NAMESPACE} delete pod ${olddriverpod}

# Build up the spark-submit command arguments as a string
sparkargs="--deploy-mode cluster --name cohortfvt-spark --class com.ibm.cohort.cql.spark.SparkCqlEvaluator local:///opt/spark/jars/cohort-evaluator-spark-1.0.2-SNAPSHOT.jar -d /cohort-config/fvt/fvt-context-definitions.json -j /cohort-config/fvt/fvt-cql-jobs.json -m /cohort-config/fvt/fvt-model-info.xml -c /cohort-config/fvt/cql --input-format delta -i Device=s3a://cohort-data-tenant2/fvt-input-data/Device -i Observation=s3a://cohort-data-tenant2/fvt-input-data/Observation -i Patient=s3a://cohort-data-tenant2/fvt-input-data/Patient -i PatientDeviceJoin=s3a://cohort-data-tenant2/fvt-input-data/PatientDeviceJoin -i Practitioner=s3a://cohort-data-tenant2/fvt-input-data/Practitioner --overwrite-output-for-contexts -o Patient=s3a://cohort-data-tenant2/fvt-output/Patient_cohort -o Observation=s3a://cohort-data-tenant2/fvt-output/Observation_cohort -o Practitioner=s3a://cohort-data-tenant2/fvt-output/Practitioner_cohort -o Device=s3a://cohort-data-tenant2/fvt-output/Device_cohort"

# use kubectl to exec into engine-spark-fvt-test pod spun off earlier and run the spark-submit command providing it the sparkargs string
kubectl -n ${CLUSTER_NAMESPACE} exec ${SPARK_POD_NAME} -- bash -c "/opt/spark/bin/spark-submit ${sparkargs}"

# call the check-spark-submit-status.sh script to find out the status of the spark-submit job.
bash tests/check-spark-submit-status.sh ${SPARK_POD_NAME}
sparksbmrc=$?
if [ ${sparksbmrc} != 0 ]; then
   echo "Spark-submit job failed. Exiting..."
   exit 1
fi

# Check to see if python3 is available for executing the xmlCombiner.py script later to combine the Results xml files into single file. If not install python3.8.
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

# Install the pyspark package for use for validating test results for the Spark/COS based tests.
pip install pyspark
echo "Install of pyspark completed successfully."

# Copy over the fvt output folder along with all subfolders from COS locally. This is so that the validation script (validateSparkFvtOutput.py) can access the output 
# files locally without having to make calls into COS. The cohort-data-tenant2 COS bucket under which fvt-output exists is already mounted in the engine-spark-fvt-test 
# pod at /spark-cos
kubectl exec -n ${CLUSTER_NAMESPACE} ${SPARK_POD_NAME} -- tar cf - /spark-cos/fvt-output | tar xf - -C .

# Call a Python script to validate the results of the spark fvt test by reading the parquet files from fvt-output folder copied earlier
$pythonBinary ${TEST_DIR}/scripts/validateSparkFvtOutput.py 

# Check for existence of a file called sparkfvttest.xml written out by the validateSparkFVTOutput.py python with validation results. If the file exists copy the file 
# into the ${OUTPUT_DIR}/Results directory to be later combined with other "results" xml files (from non Spark based tests) into a single fvttest.xml file by the 
# xmlCombiner.py script
XMLFILE=sparkfvttest.xml
if [ -f "${XMLFILE}" ]; then
    cp sparkfvttest.xml "${OUTPUT_DIR}/Results"
fi

########################################### END of section specific to SPARK based fvt tests ###############################################################

# Run the xmlCombiner.py script against all XML output copied to output/Results directory earlier.
echo "Combining all XML output into single xml file called fvttest.xml"
$pythonBinary ${TEST_DIR}/scripts/xmlCombiner.py ${OUTPUT_DIR}/Results/*.xml

# Rename the contents of the combined xml file to fvttest.xml for reporting to the DevOps Insights Console.
mv combinedResults.xml fvttest.xml