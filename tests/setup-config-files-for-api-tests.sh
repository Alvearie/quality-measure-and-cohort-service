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
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"tenantId\"/s|:.*$|: \"dft\",|" \
	-e "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-invalid-tenant.json

##########################################################################################################################

# Generate a fhir config json file where the endpoint points to the wrong port (9444)
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-badendpoint-port.json
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"tenantId\"/s|:.*$|: \"knowledge\",|" \
	-e "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT_BADPORT}\",|" ${CONFIG_DIR}/fhirconfig-badendpoint-port.json

###########################################################################################################################

# Generate a fhir config json file with the wrong password for fhiruser. (fhir user password = 'change-password')
cp ${CONFIG_DIR}/local-ibm-fhir.json ${CONFIG_DIR}/fhirconfig-knowledge-tenant-wrong-password.json
sed -i \
	-e "/\"tenantId\"/s|:.*$|: \"knowledge\",|" \
	-e "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT}\",|" ${CONFIG_DIR}/fhirconfig-knowledge-tenant-wrong-password.json

###########################################################################################################################

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
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60AndHadColonscopyFromCode\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromCode-request-data.json

############################################################################################################################################################################

# Generate over60ColonoscopyFromVS-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-request-data.json

#############################################################################################################################################################################

# Generate colColoRectalCancerScreening-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"a1637d9d-8de4-b8b8-be2e-94118c7f4d71\",|" \
	-e "/\"measureId\"/s|:.*$|: \"Measure/measure-COL_ColorectalCancerScreening-1.0.0\",|" ${CONFIG_DIR}/colColoRectalCancerScreening-request-data.json

##############################################################################################################################################################################

# Generate over60ColonoscopyFromVS-invalid-patientId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-patientId-request-data.json

###############################################################################################################################################################################

# Generate over60ColonoscopyFromVS-invalid-measureId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json
sed -i \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60AndHadColonoscopyFromVS\|1.0.0\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-measureId-request-data.json

################################################################################################################################################################################

# Generate over60ColonoscopyFromVS-invalid-fhirendpoint-port-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json
sed -i \
	-e "/\"endpoint\"/s|:.*$|: \"https:\/\/fhir-internal.dev.svc:9444\/fhir-server\/api\/v4\",|" \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json

#################################################################################################################################################################################

# Generate over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json
sed -i \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json

##################################################################################################################################################################################

# Generate over60ColonoscopyFromVS-invalid-tenantId-request-data.json
cp ${CONFIG_DIR}/measure-evaluation-api-request-data.json ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json
sed -i \
	-e "/\"tenantId\"/s|:.*$|: \"test\",|" \
	-e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientId\"/s|:.*$|: \"00ce7acb-5daa-3509-2e9f-211976bc70e1\",|" \
	-e "/\"measureId\"/s|:.*$|: \"http:\/\/ibm.com\/health\/Measure\/Over60andHadColonoscopyFromVS\|1.1.1\",|" ${CONFIG_DIR}/over60ColonoscopyFromVS-invalid-tenantId-request-data.json

###################################################################################################################################################################################

# Generate the following variations of request data json files for use by the tests specific to Evaluate Patient List Measure REST API endpoint. The json files
# are unique in terms of variations used for values of elements in the request data json object as required by different tests. Some are used for negative tests as the name of the 
# file implies.
# The files are based of the base "request_data.json" file called measure-evaluation-api-request-data.json checked into GIT repository.
#
# 1. Eval-patient-list-measure-using-measureId-request-data.json
# 2. Eval-patient-list-measure-using-identifier-plus-version-request-data.json
# 3. Eval-patient-list-measure-using-empty-patient-list-request-data.json
#
# Following files are used for negative testing
# 4. Eval-patient-list-measure-with-missing-valueset-ref-request-data.json
# 5. Eval-patient-list-measure-wrong-fhirendpoint-port-request-data.json
# 6. Eval-patient-list-measure-wrong-fhiruser-password-request-data.json
# 7. Eval-patient-list-measure-with-invalid-tenantId-request-data.json
# 8. Eval-patient-list-measure-with-invalid-patientId-request-data.json
# 9. Eval-patient-list-measure-with-blank-patientId-request-data.json
#10. Eval-patient-list-measure-with-invalid-measureId-request-data.json
######################################################################################################################################################################################

# Generate Eval-patient-list-measure-using-measureId-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-using-measureId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",\"8c25c337-ccb3-a58b-5f56-49764fa004aa\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-using-measureId-request-data.json
	
########################################################################################################################################################################################

# Generate Eval-patient-list-measure-using-identifier-plus-version-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-using-identifier-plus-version-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"00ce7acb-5daa-3509-2e9f-211976bc70e1\",\"a1637d9d-8de4-b8b8-be2e-94118c7f4d71\"],|" \
    -e "/\"measureId\"/s|:.*$|: null,|" \
	-e "/\"identifier\"/s|:.*$|: {\"value\":\"Over60-Had-Colonoscopy-From-VS\"},|" \
	-e "/\"version\"/s|:.*$|: \"1.1.1\",|" ${CONFIG_DIR}/Eval-patient-list-measure-using-identifier-plus-version-request-data.json
	
##########################################################################################################################################################################################

# Generate Eval-patient-list-measure-using-empty-patient-list-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-using-empty-patient-list-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-using-empty-patient-list-request-data.json
	
##########################################################################################################################################################################################

# Generate Eval-patient-list-measure-with-missing-valueset-ref-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-with-missing-valueset-ref-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM130-7.3.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-with-missing-valueset-ref-request-data.json
	
###########################################################################################################################################################################################

# Generate Eval-patient-list-measure-wrong-fhirendpoint-port-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-wrong-fhirendpoint-port-request-data.json
sed -i \
	-e "/\"endpoint\"/s|:.*$|: \"https:\/\/fhir-internal.dev.svc:9444\/fhir-server\/api\/v4\",|" \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",\"8c25c337-ccb3-a58b-5f56-49764fa004aa\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-wrong-fhirendpoint-port-request-data.json
	
############################################################################################################################################################################################

# Generate Eval-patient-list-measure-wrong-fhiruser-password-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-wrong-fhiruser-password-request-data.json
sed -i \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",\"8c25c337-ccb3-a58b-5f56-49764fa004aa\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-wrong-fhiruser-password-request-data.json
	
#############################################################################################################################################################################################

# Generate Eval-patient-list-measure-with-invalid-tenantId-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-tenantId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"tenantId\"/s|:.*$|: \"test\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",\"8c25c337-ccb3-a58b-5f56-49764fa004aa\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-tenantId-request-data.json
	
#############################################################################################################################################################################################

# Generate Eval-patient-list-measure-with-invalid-patientId-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-patientId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"abc-123-def-456\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-patientId-request-data.json
	
#############################################################################################################################################################################################

# Generate Eval-patient-list-measure-with-blank-patientId-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-with-blank-patientId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/EXM127-9.2.000\",|" ${CONFIG_DIR}/Eval-patient-list-measure-with-blank-patientId-request-data.json
	
#############################################################################################################################################################################################

# Generate Eval-patient-list-measure-with-invalid-measureId-request-data.json
cp ${CONFIG_DIR}/evaluate-patient-list-measure-api-request_data.json ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-measureId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: [\"302b80cd-3ef9-e663-db76-e6d397e26381\",\"a204e84b-a144-fdc5-0db4-f25d813b2cdc\",\"6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26\",\"61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",\"8c25c337-ccb3-a58b-5f56-49764fa004aa\"],|" \
    -e "/\"measureId\"/s|:.*$|: \"Measure\/ABC.1.2.300\",|" ${CONFIG_DIR}/Eval-patient-list-measure-with-invalid-measureId-request-data.json
	
##############################################################################################################################################################################################

# Generate the following variations of request data json files for use by the tests specific to Cohort Evaluation REST API endpoint. Some of the files are used for negative tests as the name of the file implies.
# The files are based of the base "request_data.json" file called cohort-evaluation-api-request-data.json checked into GIT repository.
# 1. CohortEvaluation-with-mix-of-adherent-and-nonadherent-patients-request-data.json
# 2. CohortEvaluation-with-nonadherent-patients-request-data.json
#
# Following files are used for negative testing
# 3. CohortEvaluation-with-missing-valueset-request-data.json
# 4. LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json
# 5. LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhirendpoint-port-request-data.json
# 6. LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhiruser-password-request-data.json
# 7. LungCancerScreeningWithSNOMEDcodes-cohort-invalid-tenantId-request-data.json
# 8. LungCancerScreeningWithSNOMEDcodes-cohort-patientIds-blank-request-data.json
# 9. LungCancerScreeningWithSNOMEDcodes-cohort-invalid-patientId-request-data.json
#10. LungCancerScreeningWithSNOMEDcodes-cohort-invalid-entrypoint-request-data.json
#11. LungCancerScreeningWithSNOMEDcodes-cohort-invalid-define-request-data.json

# Generate CohortEvaluation-with-mix-of-adherent-and-nonadherent-patients-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/CohortEvaluation-with-mix-of-adherent-and-nonadherent-patients-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"302b80cd-3ef9-e663-db76-e6d397e26381,6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26,8c25c337-ccb3-a58b-5f56-49764fa004aa,a204e84b-a144-fdc5-0db4-f25d813b2cdc,6b596d09-fed3-4de7-2cc6-7b62c2ee1325,61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"EXM130-7.3.000.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"Initial Population\",|" ${CONFIG_DIR}/CohortEvaluation-with-mix-of-adherent-and-nonadherent-patients-request-data.json
	
#####################################################################################################################################################################
	
# Generate CohortEvaluation-with-nonadherent-patients-request-data.json	
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/CohortEvaluation-with-nonadherent-patients-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"8c25c337-ccb3-a58b-5f56-49764fa004aa,6b596d09-fed3-4de7-2cc6-7b62c2ee1325\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"EXM130-7.3.000.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"Initial Population\",|" ${CONFIG_DIR}/CohortEvaluation-with-nonadherent-patients-request-data.json
	
######################################################################################################################################################################
	
# Generate CohortEvaluation-with-missing-valueset-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/CohortEvaluation-with-missing-valueset-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"302b80cd-3ef9-e663-db76-e6d397e26381, 6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26, 8c25c337-ccb3-a58b-5f56-49764fa004aa, a204e84b-a144-fdc5-0db4-f25d813b2cdc, 6b596d09-fed3-4de7-2cc6-7b62c2ee1325, 61b2ab7a-22af-c77e-7bf0-6ab3280b22b9\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"EXM130-7.3.000.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"Flexible Sigmoidoscopy Performed\",|" ${CONFIG_DIR}/CohortEvaluation-with-missing-valueset-request-data.json
	
#######################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-invalid-version-1-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json
	
########################################################################################################################################################################

# Generate	LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhirendpoint-port-request-data.json (port 9444)
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhirendpoint-port-request-data.json
sed -i \
    -e "/\"endpoint\"/s|:.*$|: \"${FHIR_ENDPOINT_BADPORT}\",|" \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhirendpoint-port-request-data.json
	
##########################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhiruser-password-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhiruser-password-request-data.json
sed -i \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhiruser-password-request-data.json
	
###########################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-invalid-tenantId-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-tenantId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"tenantId\"/s|:.*$|: \"test\"|" \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-tenantId-request-data.json
	
############################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-patientIds-blank-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-patientIds-blank-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-patientIds-blank-request-data.json	
	
#############################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-invalid-patientId-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-patientId-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"abc123\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-patientId-request-data.json
	
#############################################################################################################################################################################

# Generate LungCancerScreeningWithSNOMEDcodes-cohort-invalid-entrypoint-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-entrypoint-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"abc123-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"MeetsInclusionCriteria\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-entrypoint-request-data.json
	
##############################################################################################################################################################################
	
# Generate LungCancerScreeningWithSNOMEDcodes-cohort-invalid-define-request-data.json
cp ${CONFIG_DIR}/cohort-evaluation-api-request_data.json ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-define-request-data.json
sed -i \
    -e "/\"password\"/s|:.*$|: \"${FHIR_USER_PASS}\",|" \
	-e "/\"patientIds\"/s|:.*$|: \"9da5f632-a80c-6614-2e62-bd695449654d,d6587935-b9e4-188d-7e9f-a8dc909f4216,5d507fe1-e386-36a9-1d51-13a996ae66bf\",|" \
	-e "/\"entrypoint\"/s|:.*$|: \"LungCancerScreeningWithSNOMEDcodes-1.0.1.cql\",|" \
	-e "/\"defineToRun\"/s|:.*$|: \"InPop\",|" ${CONFIG_DIR}/LungCancerScreeningWithSNOMEDcodes-cohort-invalid-define-request-data.json
	
###############################################################################################################################################################################


