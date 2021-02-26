#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################################
# Export the following Environment variables so that they are accessible to the child script 
# run-engine-taurus-deploy.sh invoked from this script.                   
##############################################################################################
export TRUSTSTORE_PASSWORD="TStore-Password"
export TRUSTSTORE_TYPE="pkcs12"
export TRUSTSTORE="tests\/src\/main\/resources\/config\/trustStore.pkcs12"
export FHIR_USER_PASS="${FHIR_USER_PASSWORD}"

cd tests

# call the run-taurus-deploy.sh script passing in the final xml output file name (-o option) and a comma separated list of test yaml files (-t file1, file2 ..)
bash ./run-engine-taurus-deploy.sh -o fvttest.xml -t tests/scenarios/breastCancerTestScenarios.yaml,tests/scenarios/coloRectalCancerTestScenarios.yaml,tests/scenarios/lungCancerTestScenarios.yaml,tests/scenarios/measureCLIExample-separate-measure-server.yaml
