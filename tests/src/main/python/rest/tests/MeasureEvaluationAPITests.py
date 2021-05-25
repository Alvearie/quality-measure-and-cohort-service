from __future__ import absolute_import
import sys
import os
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.measure_evaluation_api import MeasureEvaluationApi
from swagger_client.rest import ApiException

# Set global variables in the file that can be accessed by all tests within the class
reqdata_path = "/bzt-configs/tests/src/main/resources/config/"
measurezip_path = "/bzt-configs/tests/src/main/resources/cql/measure-zip/"

class MeasureEvaluationAPITests(TestClass):

    def test_evaluateMeasureWithSNOMEDCodes(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromCode-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_code_v1_1_1.zip"
        resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Measure report returned.'
        expectedResp = '{"resourceType":"MeasureReport","extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valuePeriod":{"start":"2019-03-18T00:00:00.000+00:00","end":"2020-09-18T23:59:59.999+00:00"}}],"name":"Measurement Period","use":"in","type":"Period"}},{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valueString":"ProductLine"}],"name":"Product Line","use":"in","type":"string"}}],"status":"complete","type":"individual","measure":"Measure/Over60AndHadColonscopyFromCode-1.1.1","subject":{"reference":"Patient/00ce7acb-5daa-3509-2e9f-211976bc70e1"},"period":{"start":"2019-03-18T00:00:00.000+00:00","end":"2020-09-18T23:59:59.999+00:00"},"group":[{"population":[{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"initial-population"}]},"count":1},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"numerator"}]},"count":1},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"denominator"}]},"count":1}],"measureScore":{"value":1.0}}]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp
    
    def test_evaluateMeasureWithValueSets(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Measure report returned'
        expectedResp = '{"resourceType":"MeasureReport","extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valuePeriod":{"start":"2019-03-18T00:00:00.000+00:00","end":"2020-09-18T23:59:59.999+00:00"}}],"name":"Measurement Period","use":"in","type":"Period"}},{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valueString":"ProductLine"}],"name":"Product Line","use":"in","type":"string"}}],"status":"complete","type":"individual","measure":"Measure/Over60andHadColonoscopyFromVS-1.1.1","subject":{"reference":"Patient/00ce7acb-5daa-3509-2e9f-211976bc70e1"},"period":{"start":"2019-03-18T00:00:00.000+00:00","end":"2020-09-18T23:59:59.999+00:00"},"group":[{"population":[{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"initial-population"}]},"count":1},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"numerator"}]},"count":1},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"denominator"}]},"count":1}],"measureScore":{"value":1.0}}]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp

    def test_evaluateMeasureWithMissingValueSetOnTenant(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "colColoRectalCancerScreening-request-data.json"
        measure = measurezip_path + "col_colorectal_cancer_screening_1.0.0.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithMissingValueSetOnTenant is : " + result)
        assert '400' in result, 'Should contain 400 error due to IllegalArgumentException caught due to unresolved value set reference.'
    
    def test_evaluateMeasureWithInvalidPatientIdValue(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-invalid-patientId-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithInvalidPatientIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error due to exception communicating with FHIR server as specified PatientId not found on server.'

    def test_evaluateMeasureWithInvalidMeasureIdValue(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-invalid-measureId-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithInvalidMeasureIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Failed to resolve the specified Measure resource.'

    def test_evaluateMeasureWithIncorrectMeasureFileFormat(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.json"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithIncorrectMeasureFileFormat is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Failed to resolve the specified Measure resource.'

    def test_evaluateMeasureWithMissingDependenciesZipFile(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1_incomplete.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithMissingDependenciesZipFile is : " + result)
        assert '400' in result, 'Should contain 400 error due to unexpected exception caught as a required library resource was not found.'

    def test_evaluateMeasureWithWrongFHIREndpointPort(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-wrong-fhirendpoint-port-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithWrongFHIREndpointPort is : " + result)
        assert '500' in result, 'Should contain 500 Error message stating: Connect to fhir-internal.dev.svc:9444 failed.'

    def test_evaluateMeasureWithWrongFHIRUserPassword(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-wrong-fhiruser-password-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithWrongFHIRUserPassword is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: HTTP 401 Unauthorized.'

    def test_evaluateMeasureWithInvalidTenantIdValue(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "over60ColonoscopyFromVS-invalid-tenantId-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithInvalidTenantIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: FHIRPersistenceException: Unexpected exception while creating JDBC persistence layer.'


    def test_evaluateMeasureWithInvalidAPIVersion1(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '2021/04/24'
        request_data = reqdata_path + "over60ColonoscopyFromVS-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithInvalidAPIVersion1 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'

    def test_evaluateMeasureWithInvalidAPIVersion2(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '04-24-2021'
        request_data = reqdata_path + "over60ColonoscopyFromVS-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_from_vs_v1_1_1.zip"
        try:
            resp = measureEvaluationApi.evaluate_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithInvalidAPIVersion2 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'