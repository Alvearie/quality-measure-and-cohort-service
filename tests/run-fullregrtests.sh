#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
set -x

passing=0
failing=0
output=""

#podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}-${GIT_BRANCH}-${CHART_NAME}" | grep Running | cut -d " " -f 1 | head -n1)
podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}" | grep Running | cut -d " " -f 1 | head -n1)

echo "### serviceState ###"
test1=$(kubectl exec --namespace="${CLUSTER_NAMESPACE}" $podname -- curl http://localhost:9080/services/cohort/api/v1/status?liveness_check=false)
if echo "$test1" | grep -q '"serviceState":"OK"';then
 echo "[PASS] serviceState"
  passing=$((passing+1))
  output="$output\n<testcase classname=\"bash\" name=\"serviceState\" time=\"0\"/>"
else
  failing=$((failing+1))
  output="$output\n<testcase classname=\"bash\" name=\"serviceState\" time=\"0\"><failure>fail</failure><expected>OK</expected><actual>$test1</actual></testcase>"
fi

echo "### Ensure port 443 is not active ###"
set +e
test1=$(kubectl exec --namespace="${CLUSTER_NAMESPACE}" $podname -- curl http://localhost:443/services/cohort/api/v1/status?liveness_check=false 2>&1)
set -e
if echo "$test1" | egrep -q "Connection refused|command terminated with exit code 4";then
 echo "[PASS] 443 port is no active"
  passing=$((passing+1))
  output="$output\n<testcase classname=\"bash\" name=\"port 443 not active\" time=\"0\"/>"
else
  failing=$((failing+1))
  output="$output\n<testcase classname=\"bash\" name=\"port 443 not active\" time=\"0\"><failure>fail</failure><expected></expected><actual>$test1</actual></testcase>"
fi

echo
echo "----------------"
echo "Final Results"
echo "----------------"
echo "PASSING: $passing"
echo "FAILING: $failing"
total=$(($passing + $failing))

date=`date`
header="<testsuite name=\"Regression tests\" tests=\"$total\" failures=\"$failing\" errors=\"$failing\" skipped=\"0\" timestamp=\"${date}\" time=\"0\">"
footer="</testsuite>"

filename="fullregtest.xml"
cat << EOF > $filename
$header
$output
$footer
EOF

if [ $failing -gt 0 ]; then
  exit 1
fi
