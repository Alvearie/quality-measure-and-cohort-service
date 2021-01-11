set -x

passing=0
failing=0
output=""

#podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}-${GIT_BRANCH}-${CHART_NAME}" | grep Running | cut -d " " -f 1 | head -n1)
podname=$(kubectl get pods --namespace "${CLUSTER_NAMESPACE}" | grep -i "${APP_NAME}-tenant-id" | grep Running | cut -d " " -f 1 | head -n1)

echo "### Welcome Message ###"
test1=$(kubectl exec --namespace="${CLUSTER_NAMESPACE}" $podname -- wget -qO- http://127.0.0.1:8088)
if echo "$test1" | grep -q Welcome;then
 echo "[PASS] Welcome Message"
  passing=$((passing+1))
  output="$output\n<testcase classname=\"bash\" name=\"welcome message\" time=\"0\"/>"
else
  failing=$((failing+1))
  output="$output\n<testcase classname=\"bash\" name=\"welcome message\" time=\"0\"><failure>fail</failure><expected>Welcome</expected><actual>$test1</actual></testcase>"
fi

echo
echo "----------------"
echo "Final Results"
echo "----------------"
echo "PASSING: $passing"
echo "FAILING: $failing"
total=$(($passing + $failing))


echo "----------------"
echo "Writing to xunit output"
echo "----------------"

date=`date`
header="<testsuite name=\"Smoketests\" tests=\"$total\" failures=\"$failing\" errors=\"$failing\" skipped=\"0\" timestamp=\"${date}\" time=\"0\">"
footer="</testsuite>"

filename="smoketests.xml"
cat << EOF > $filename
$header
$output
$footer
EOF


if [ $failing -gt 0 ]; then
  exit 1
fi
