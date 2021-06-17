# file used to create dummy tests for wh-cohorting-deploy umbrella repo so we don't run fvt tests during customer/staging/production deployments
# referenced from pre/postPromotion.sh scripts
output="<testcase classname=\"bash\" name=\"test1\" time=\"0\"/>"
date=`date`
header="<testsuite name=\"FVT tests\" tests=\"1\" failures=\"0\" errors=\"0\" skipped=\"0\" timestamp=\"${date}\" time=\"0\">"
footer="</testsuite>"

filename=$1
cat << EOF > $filename
$header
$output
$footer
EOF