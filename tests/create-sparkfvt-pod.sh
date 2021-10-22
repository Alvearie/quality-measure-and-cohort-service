#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

SPARK_POD_NAME=$1
# Get the deployment file ready for the engine-spark-fvt-test pod that will be spun off. Replace placeholder fields with actual values.
cp ${TEST_DIR}/run-spark-fvt-deploy.yaml ${TEST_DIR}/run-spark-fvt-deploy-with-replaced-values.yaml
sed -i "s/CLUSTER_NAMESPACE_PLACEHOLDER/${CLUSTER_NAMESPACE}/g" ${TEST_DIR}/run-spark-fvt-deploy-with-replaced-values.yaml
sed -i "s/POD_NAME_PLACEHOLDER/${SPARK_POD_NAME}/g" ${TEST_DIR}/run-spark-fvt-deploy-with-replaced-values.yaml

#Retrieve the spark kubenetes container image name pushed by the CI toolchain during current run to replace placeholder in spark-defaults.conf file
if [ -f tests/sparkimage.txt ]; then
  sparkImage=$(cat tests/sparkimage.txt)
  echo "sparkimage pushed to image container registry by CI toolchain was: ${sparkImage}"
fi

#Replace the placeholder spark kubernetes container image value in spark-defaults.conf file with one built by the CI toolchain
sed -i "s_^spark\.kubernetes\.container\.image .*_spark.kubernetes.container.image ${sparkImage}_g" tests/src/main/resources/sparkconf/spark-defaults.conf

#Replace the placeholder spark kubernetes namespace value in spark-defaults.conf file with the value of ${CLUSTER_NAMESPACE}
sed -i "s_^spark\.kubernetes\.namespace .*_spark.kubernetes.namespace ${CLUSTER_NAMESPACE}_g" tests/src/main/resources/sparkconf/spark-defaults.conf

#Delete the engine-spark-fvt-test from previous run before spinning off a new one
kubectl -n ${CLUSTER_NAMESPACE} delete pod/${SPARK_POD_NAME}

# Spin off the new engine-spark-fvt-test pod to run SPARK based tests
echo "Creating spark fvt test pod: ${SPARK_POD_NAME}"
kubectl apply -f ${TEST_DIR}/run-spark-fvt-deploy-with-replaced-values.yaml
sleep 15  # Sleep for 15 seconds before checking if the deployed pod is running
podStatus=$(kubectl get pods -n "${CLUSTER_NAMESPACE}" --field-selector=status.phase=Running | grep ${SPARK_POD_NAME})
if [ -z "${podStatus}" ]
then
  echo "${SPARK_POD_NAME} pod is not running as expected. Exiting ..."
  exit -1
fi

#Lastly copy the spark-defaults.conf and pod-cohort-evaluator-spark.yaml files into the running pod's /opt/spark/conf directory
kubectl cp tests/src/main/resources/sparkconf/spark-defaults.conf -n ${CLUSTER_NAMESPACE} engine-spark-fvt-test:/opt/spark/conf
kubectl cp tests/src/main/resources/sparkconf/pod-cohort-evaluator-spark.yaml -n ${CLUSTER_NAMESPACE} engine-spark-fvt-test:/opt/spark/conf