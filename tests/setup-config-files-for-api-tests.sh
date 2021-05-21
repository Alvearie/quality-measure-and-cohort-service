#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#!/bin/bash

# Get common test setup environment variables
. tests/setupEnvironmentVariables.sh

# Create the following three FHIR json files for Negative testing of REST API Endpoints

# Generate fhir config json file that has an invalid tenantId (tenantId that is not configured on the FHIR server)
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-invalid-tenant.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/fhirconfig-invalid-tenant.json
sed -i "/\"tenantId\"/s|:.*$|: \"dft\",|" ${CONFIG_DIR}/fhirconfig-invalid-tenant.json
sed -i "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-invalid-tenant.json

# Generate a fhir config json file where the endpoint points to the wrong port (9444)
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-badendpoint-port.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/fhirconfig-badendpoint-port.json
sed -i "/\"tenantId\"/s|:.*$|: \"default\",|" ${CONFIG_DIR}/fhirconfig-badendpoint-port.json
sed -i "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT_BADPORT}\",|" ${CONFIG_DIR}/fhirconfig-badendpoint-port.json

# Generate a fhir config json file with the wrong password for fhiruser. (fhir user password = 'change-password')
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-default-tenant-wrong-password.json
sed -i "/\"tenantId\"/s|:.*$|: \"default\",|" ${CONFIG_DIR}/fhirconfig-default-tenant-wrong-password.json
sed -i "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-default-tenant-wrong-password.json

# Generate the following variations of request data json files for use by the tests specific to Measure Evaluation REST API endpoint. The json files
# are unique in terms of the measureId and/or patientId referenced in them. Some are used for negative tests as the name of the file implies.
# The files are based of the base "request_data.json" file called measure-evaluation-api-request-data.json checked into GIT repository.
# 1. over60ColonoscopyFromCode-request-data.json
# 2. over60ColonoscopyFromVS-request-data.json
# 3. colColoRectalCancerScreening-request-data.json
#
# Following files are used for negative testing
# 4. over60ColonoscopyFromVS-invalid-patientId-request-data.json
# 5. over60ColonoscopyFromVS-invalid-measureId-request-data.json
# 6. over60ColonoscopyFromVS-invalid-fhirendpoint-port-request-data.json
# 7. over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
# 8. over60ColonoscopyFromVS-invalid-tenantId-request-data.json

# Generate over60ColonoscopyFromCode-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromCode-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromCode-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromCode-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60AndHadColonscopyFromCode\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromCode-request-data.json

# Generate over60ColonoscopyFromVS-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json

# Generate colColoRectalCancerScreening-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"a1637d9d-8de4-b8b8-be2e-94118c7f4d71\",|" ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"Measure/measure-COL_ColorectalCancerScreening-1.0.0\",|" ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json

# Generate over60ColonoscopyFromVS-invalid-patientId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json

# Generate over60ColonoscopyFromVS-invalid-measureId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60AndHadColonoscopyFromVS\|1.0.0\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json

# Generate over60ColonoscopyFromVS-invalid-fhirendpoint-port-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json
sed -i "/\"endpoint\"/s|:.*$|: \"https:\/\/fhir-internal.dev.svc:9444\/fhir-server\/api\/v4\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json

# Generate over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json

# Generate over60ColonoscopyFromVS-invalid-tenantId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json
sed -i "/\"tenantId\"/s|:.*$|: \"test\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json
sed -i "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json
sed -i "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json
sed -i "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json