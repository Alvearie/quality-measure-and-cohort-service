#!/bin/sh
CLOUD_API_KEY=$1
REGION=$2
CLUSTER_RESOURCE_GROUP=$3
CLUSTER_NAME=$4
CLUSTER_NAMESPACE=$5

if [ -z $CLOUD_API_KEY ] || [ -z $REGION ] || [ -z $CLUSTER_RESOURCE_GROUP ] || [ -z $CLUSTER_NAME ] || [ -z $CLUSTER_NAMESPACE ]; then
  echo "Usage: source $0 apiKey region clusterResourceGroup clusterName namespaceToUse"
  exit 1
fi

echo ""
echo "*********************************************"
echo "* ibmcloud login step *"
echo "*********************************************"
export IBMCLOUD_VERSION_CHECK=false
ibmcloud login --apikey ${CLOUD_API_KEY} -r ${REGION} -g ${CLUSTER_RESOURCE_GROUP}

echo ""
echo "*********************************************"
echo "* step to export the environment *"
echo "*********************************************"
ibmcloud ks cluster config --cluster ${CLUSTER_NAME}

kubectl config set-context ${CLUSTER_NAME} --namespace=$CLUSTER_NAMESPACE

echo ""
echo "exiting"

kubectl -n $CLUSTER_NAMESPACE port-forward service/fhir-internal 9443:9443 &
