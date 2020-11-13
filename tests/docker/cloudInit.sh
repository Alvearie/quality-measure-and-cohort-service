#
#
# note ...
# must run as " . ./script.sh"
# that is, if you don't start with a dot then the
# environment export does not work.
#
export MY_CLUSTER=$2
export MY_GROUP=$3
export MY_NAMESPACE=$4
MY_API_KEY=$1
API_URL=https://cloud.ibm.com

if [ -z $MY_CLUSTER ] || [ -z $MY_GROUP ] || [ -z $MY_NAMESPACE ] || [ -z $MY_API_KEY ]; then
  echo "Usage: source $0 apiKey clusterName clusterGroupName namespaceToUse"
  exit 1
fi

echo ""
echo "*********************************************"
echo "* ibmcloud login step *"
echo "*********************************************"
export IBMCLOUD_VERSION_CHECK=false
ibmcloud api ${API_URL}

ibmcloud login --apikey ${MY_API_KEY} -r us-south -g $MY_GROUP

echo ""
echo "*********************************************"
echo "* step to export the environment *"
echo "*********************************************"
ibmcloud ks cluster config --cluster ${MY_CLUSTER}

kubectl config set-context ${MY_CLUSTER} --namespace=$MY_NAMESPACE
kubectl config set-context --current --namespace=$MY_NAMESPACE

echo ""
echo "exiting"
kubectl get pod -n $MY_NAMESPACE | grep Running
kubectl -n cdt-commops-quickteam01-ns-01 port-forward service/fhir-internal 9443:9443 &
