#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
##############################################################################################
# Export the following Environment variables so that they are accessible to various
# testing scripts.
##############################################################################################
export TRUSTSTORE_PASSWORD="${FHIR_TRUSTSTORE_PASSWD}"
export TRUSTSTORE_TYPE="pkcs12"
export TRUSTSTORE="tests/src/main/resources/config/trustStore.pkcs12"
export FHIR_USER_PASS="${FHIR_USER_PASSWORD}"

# Set up common paths assuming toolchain runs one directory above `tests` in umbrella repo
export TEST_DIR=tests/
export CONFIG_DIR=tests/src/main/resources/config
export LIB_DIR=tests/src/main/resources/libraries
export SCENARIOS_DIR=tests/scenarios
export OUTPUT_DIR=tests/output

# Set up Environment variables to use later in this script where the test scenario files need to be updated to reflect the current test 
# environment. 
export DEFAULT_TENANT="tests/src/main/resources/config/fhirconfig-default-tenant.json"
export TESTFVT_TENANT="tests/src/main/resources/config/fhirconfig-testfvt-tenant.json"

# check if FHIR_CLUSTER_NAMESPACE has been set in the toolchain
# properties and use it if it is there, otherwise assume fhir will be
# running in the same namespace
if [ -z "$FHIR_CLUSTER_NAMESPACE ]
then
	FHIR_CLUSTER_NAMESPACE=${CLUSTER_NAMESPACE}
else
	FHIR_CLUSTER_NAMESPACE=${FHIR_CLUSTER_NAMESPACE}

export FHIR_CLUSTER_NAMESPACE
export FHIR_ENDPOINT="https://fhir-internal.${FHIR_CLUSTER_NAMESPACE}.svc:9443/fhir-server/api/v4"
export SHADED_JAR="/bzt-configs/tests/src/main/resources/libraries/cohort-cli-shaded.jar"