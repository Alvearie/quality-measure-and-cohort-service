#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

SPARK_POD_NAME=$1
DVRPODPREFIX="cohortfvt-spark"
driverpod=`kubectl -n ${CLUSTER_NAMESPACE} get pods --no-headers -o custom-columns=":metadata.name" | grep ${DVRPODPREFIX} | grep driver`
echo "Driver pod is: ${driverpod}"
echo "Getting status of driver pod: ${driverpod}"
status=`kubectl -n ${CLUSTER_NAMESPACE} get pod ${driverpod} --no-headers -o custom-columns=":status.phase"`
echo "driver pod status is: ${status}"
count=1
while ( [ "${status}" == "Pending" ] || [ "${status}" == "Running" ] ) && [ ${count} le 30 ] 
do
   echo "Polling again for the status of driver pod after a wait of 10 seconds"
   sleep 10
   status=`kubectl -n ${CLUSTER_NAMESPACE} get ${driverpod} --no-headers -o custom-columns=":status.phase"`
   echo "driver pod status is: ${status} - count is : ${count}"
   ${count}=${count}+1
done

if [ "${status}" != "Succeeded" ]; then 
   echo "Spark Driver Pod reported status of: ${status}"
   exit 1
fi

echo "Executing spark-submit --status on engine-spark-fvt-test pod and writing out the status log to file sparksbmsts.txt file."
kubectl -n ${CLUSTER_NAMESPACE} exec ${SPARK_POD_NAME} -- bash -c "/opt/spark/bin/spark-submit --status ${CLUSTER_NAMESPACE}:${driverpod} --master k8s://https://c105.us-east.containers.cloud.ibm.com:32327 2> /tmp/sparksbmsts.txt"
#Copying the status log file from the ${SPARK_POD_NAME} filesystem to a local directory.
kubectl -n ${CLUSTER_NAMESPACE} cp ${SPARK_POD_NAME}:/tmp/sparksbmsts.txt sparksbmsts.txt
#Extracting the exit code from the spark-submit job status log
exitcode="`grep "exit code" sparksbmsts.txt | tr -d ' ' | cut -d':' -f2`"
echo "spark-submit exit code is: ${exitcode}"

if [ "${exitcode}" != 0 ]; then 
   echo "Spark-submit command ended in Error."
   exit 2
fi 