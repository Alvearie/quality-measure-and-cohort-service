set -x

passing=0
failing=0
output=""

#podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}-${GIT_BRANCH}-${CHART_NAME}" | grep Running | cut -d " " -f 1 | head -n1)
podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}-tenant-id" | grep Running | cut -d " " -f 1 | head -n1)

echo "### Ensure Toolchain Test Service is in Response Message ###"
test1=$(kubectl exec --namespace="${CLUSTER_NAMESPACE}" $podname -- wget -qO- http://127.0.0.1:8088)
if echo "$test1" | grep -q "Toolchain Test Service";then
 echo "[PASS] Toolchain Test Service Message"
  passing=$((passing+1))
  output="$output\n<testcase classname=\"bash\" name=\"toolchain test\" time=\"0\"/>"
else
  failing=$((failing+1))
  output="$output\n<testcase classname=\"bash\" name=\"toolchain test\" time=\"0\"><failure>fail</failure><expected></expected><actual>$test1</actual></testcase>"
fi

echo "### Ensure port 443 is not active ###"
set +e
test1=$(kubectl exec --namespace="${CLUSTER_NAMESPACE}" $podname -- wget -qO- https://127.0.0.1:443 2>&1)
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

filename="regtest.xml"
cat << EOF > $filename
$header
$output
$footer
EOF

if [ $failing -gt 0 ]; then
  exit 1
fi
