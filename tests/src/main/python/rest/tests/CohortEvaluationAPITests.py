from __future__ import absolute_import
import sys
import os
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.cohort_evaluation_api import CohortEvaluationApi
from swagger_client.rest import ApiException

# Set global variables in the file that can be accessed by all tests within the class
reqdata_path = "/bzt-configs/tests/src/main/resources/config/"
cqldefnzip_path = "/bzt-configs/tests/src/main/resources/cql/cqldefn-zip/"

class CohortEvaluationAPITests(TestClass):

    def test_evaluateCohortWithMixOfAdherentAndNonAdherentPatients(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "CohortEvaluation-with-mix-of-adherent-and-nonadherent-patients-request-data.json"
        cqldefn = cqldefnzip_path + "EXM130-7.3.000.zip"
        resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Result should return list of patientIds that are adherent for the evaluated cohort.'
        expectedResp = '{"result":["302b80cd-3ef9-e663-db76-e6d397e26381","6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26","a204e84b-a144-fdc5-0db4-f25d813b2cdc","61b2ab7a-22af-c77e-7bf0-6ab3280b22b9"]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp
	
    def test_evaluateCohortWithNonAdherentPatients(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "CohortEvaluation-with-nonadherent-patients-request-data.json"
        cqldefn = cqldefnzip_path + "EXM130-7.3.000.zip"
        resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Result should return and empty list.'
        expectedResp = '{"result":[]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp

    def test_evaluateCohortWithMissingValueSet(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "CohortEvaluation-with-missing-valueset-request-data.json"
        cqldefn = cqldefnzip_path + "EXM130-7.3.000.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithMissingValueSet is : " + result)
        assert '400' in result, 'Should contain 400 error due IllegalArgumentException with the error message stating: Could not resolve value set.'

    def test_evaluateCohortWithInvalidAPIVersion1(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '2021/07/26'
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidAPIVersion1 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'

    def test_evaluateCohortWithInvalidAPIVersion2(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '07-26-2021'
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidAPIVersion2 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'

    def test_evaluateCohortWithWrongFHIREndpointPort(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhirendpoint-port-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithWrongFHIREndpointPort is : " + result)
        assert '400' in result, 'Should contain a 400 error due to FHIRClientConnectException with the error message stating: Failed to retrieve the server metadata statement during client initialization.'

    def test_evaluateCohortWithWrongFHIRUserPassword(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-wrong-fhiruser-password-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithWrongFHIRUserPassword is : " + result)
        assert '400' in result, 'Should contain a 400 error due to Authentication exception with the error message stating: HTTP 401 Unauthorized.'


    def test_evaluateCohortWithInvalidTenantIdValue(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-invalid-tenantId-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidTenantIdValue is : " + result)
        assert '400' in result, 'Should contain a 400 error with the error message stating: InvalidRequestException: HTTP 400 Bad Request'


    def test_evaluateCohortWithPatientIdsBlank(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-patientIds-blank-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithPatientIdsBlank is : " + result)
        assert '400' in result, 'Should contain a 400 error due to IllegalArgumentException with the error message stating: The ID can not be blank.'

    def test_evaluateCohortWithInvalidPatientId(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-invalid-patientId-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidPatientId is : " + result)
        assert '400' in result, 'Should contain a 400 error due to ResourceNotFoundException with the error message stating: HTTP 404 Not Found.'

    def test_evaluateCohortWithInvalidEntrypoint(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-invalid-entrypoint-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidEntrypoint is : " + result)
        assert '400' in result, 'Should contain a 400 error with the error message stating: No library resource found for the specified entrypoint'

    def test_evaluateCohortWithInvalidDefine(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-invalid-define-request-data.json"
        cqldefn = cqldefnzip_path + "lung_cancer_screening_with_snomed_codes_v1_0_1.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidDefine is : " + result)
        assert '400' in result, 'Should contain a 400 error with the error message stating: Could not resolve expression reference <specified define> in library.'
			
    def test_evaluateCohortWithInvalidZipFile(self):
        cohortEvaluationApi = CohortEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "LungCancerScreeningWithSNOMEDcodes-cohort-request-data.json"
        cqldefn = cqldefnzip_path + "EXM130-7.3.000.zip"
        try:
            resp = cohortEvaluationApi.evaluate_cohort(version, request_data, cqldefn, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateCohortWithInvalidZipFile is : " + result)
        assert '400' in result, 'Should contain a 400 error with the error message stating: No library source found for the specified cql definition.'

  