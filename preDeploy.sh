#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
set -xe
echo "Executing preDeploy.sh"

echo "IDS_PROJECT_NAME=${IDS_PROJECT_NAME}"

# TODO - tabishop the commented out lines below originated in the whc-starter-app that
# is the suggested template for new IBM toolchain projects. We still have work to set
# up our helm chart overrides for our CSP environments, but it doesn't make sense to
# do that until we know how we will be passing in the config for fhir servers and tenants.
# Revisit this once we have more clarity on the config that needs to get passed in and are able
# to set up our helm chart overrides
#templateBranch="master"
#if echo $Build_Prefix | egrep "^(\S*)-SCD$";then
#  curl -sSL -u "watkins0@us.ibm.com:${gitApiKey}" "https://raw.github.ibm.com/whc-toolchain/njss-ops-overrides/${templateBranch}/${CLUSTER_NAME}/override.yaml" > chart/nodejs-starter/override.yaml
#elif echo $Build_Prefix | egrep "^(\S*)-SCVV$";then
#  curl -sSL -u "watkins0@us.ibm.com:${gitApiKey}" "https://raw.github.ibm.com/whc-toolchain/njss-ops-overrides/${templateBranch}/${CLUSTER_NAME}/override.yaml" > chart/nodejs-starter/override.yaml
#elif echo $Build_Prefix | egrep "^(\S*)-PCD$";then
#  curl -sSL -u "watkins0@us.ibm.com:${gitApiKey}" "https://raw.github.ibm.com/whc-toolchain/njss-ops-overrides/${templateBranch}/${CLUSTER_NAME}/override.yaml" > chart/nodejs-starter/override.yaml
#else
#  curl -sSL -u "watkins0@us.ibm.com:${gitApiKey}" "https://raw.github.ibm.com/whc-toolchain/njss-dev-overrides/${templateBranch}/${CLUSTER_NAME}/override.yaml" > chart/nodejs-starter/override.yaml
#fi
#cat chart/nodejs-starter/override.yaml
#ls -la chart/nodejs-starter/

# get this value from KP-CDT-SAMPLE Keyprotect instance
# export REPLCOUNT="1"

if echo "$PIPELINE_TOOLCHAIN_ID" | egrep "430afb18-4418-4ce9-95ea-5980bbaa7760"; then
  exit 1
fi

#if ! [ -z "$OC_SERVER_URL" ]; then
#  deployfile="chart/nodejs-starter/templates/deployment-new.yaml"
#  sed -e 's/nodeSelector.*$//' -e 's/worker-type.*application$//' ${deployfile} > /tmp/temp.yaml 2>*1
#  cp -fv /tmp/temp.yaml ${deployfile}
#  cat ${deployfile}
#fi