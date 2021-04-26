#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

POD_NAME=$1
echo "Creating test pod: ${POD_NAME}"

# Make sure common test setup environment variables are available
. tests/setupEnvironmentVariables.sh

# Get the deployment file ready for the pod that will be spun off.
cp ${TEST_DIR}/run-engine-taurus-deploy.yaml ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/CLUSTER_NAMESPACE_PLACEHOLDER/${CLUSTER_NAMESPACE}/g" ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/POD_NAME_PLACEHOLDER/${POD_NAME}/g" ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s|TRUSTSTORE_PLACEHOLDER|${TRUSTSTORE}|" ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml
sed -i "s/TRUSTSTORE_TYPE_PLACEHOLDER/${TRUSTSTORE_TYPE}/" ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml

kubectl -n ${CLUSTER_NAMESPACE} delete pod/${POD_NAME}

# Get the cohort-cli shaded jar from the cohort-services pod.
COHORTSVC_POD=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}" | grep Running | cut -d " " -f 1 | head -n1)
echo "Using jar from pod ${COHORTSVC_POD}" 
kubectl -n ${CLUSTER_NAMESPACE} cp ${COHORTSVC_POD}:/opt/alvearie/cohortEngine/cohort-cli-shaded.jar ${LIB_DIR}/cohort-cli-shaded.jar
echo "Copied cohort-cli shaded jar from the running cohort-services pod."

# Before tests can be executed prepare the test yaml files to have the right values replaced in them  

# Generate fhir config json file for test-fvt tenant
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-testfvt-tenant.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/fhirconfig-testfvt-tenant.json
sed -i "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-testfvt-tenant.json

# Generate fhir config json file for default tenant
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-default-tenant.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/fhirconfig-default-tenant.json
sed -i "/\"tenantId\"/s|:.*$|: \"default\",|" ${CONFIG_DIR}/fhirconfig-default-tenant.json
sed -i "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-default-tenant.json

# Spin off the pod in which the taurus image will be executed
kubectl apply -f ${TEST_DIR}/run-engine-taurus-deploy-with-replaced-values.yaml

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
kubectl -n ${CLUSTER_NAMESPACE} exec ${POD_NAME} -- bash -c "export TRUSTSTORE_PASSWORD=${TRUSTSTORE_PASSWORD} TRUSTSTORE=${TRUSTSTORE} CLUSTER_NAMESPACE=${CLUSTER_NAMESPACE} FHIR_CLUSTER_NAMESPACE=${FHIR_CLUSTER_NAMESPACE} && /bzt-configs/tests/createTrustStore.sh"
