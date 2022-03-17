#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
set -xe
echo "Executing preDeploy.sh"

source /whc-commons/scripts/common-functions/utilities.sh
# Description: Add specified environment variable and value to the
#              build properties file that is sourced by each
#              subsequent step in the pipeline task being run.
#              This allows solution scripts for pre and post actions, and
#              tests to pass environment variables on to the scripts called
#              in steps that come after.
#              The "export varName="varValue"" line is appended to the current
#              build.properties.src file, effectively adding OR overriding
#              (if already set) the environment variable.
addEnvVar varname "FHIR_CLUSTER_NAMESPACE"
# Export the variable here also if you call scripts from this script that might need it OR if the toolchain script for the step (e.g. deploy_helm.sh) looks up the value after invoking your script (e.g. preDeploy.sh)
export FHIR_CLUSTER_NAMESPACE="dev"
