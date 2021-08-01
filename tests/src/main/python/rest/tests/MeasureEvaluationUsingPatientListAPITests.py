from __future__ import absolute_import
import sys
import os
import re
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.measure_evaluation_api import MeasureEvaluationApi
from swagger_client.rest import ApiException

# Set global variables in the file that can be accessed by all tests within the class
reqdata_path = "/bzt-configs/tests/src/main/resources/config/"
measurezip_path = "/bzt-configs/tests/src/main/resources/cql/measure-zip/"

class MeasureEvaluationUsingPatientListAPITests(TestClass):
    
    def test_evaluatePatientListMeasureUsingMeasureId(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-using-measureId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Measure report returned'
        expectedResp = '{"resourceType":"MeasureReport","contained":[{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/302b80cd-3ef9-e663-db76-e6d397e26381","display":"Mr. Agustin437 Wyman904"}},{"item":{"reference":"Patient/61b2ab7a-22af-c77e-7bf0-6ab3280b22b9","display":"Mr. Alfredo17 Gaylord332"}},{"item":{"reference":"Patient/6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26","display":"Mr. Al123 Marquardt819"}}]},{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/302b80cd-3ef9-e663-db76-e6d397e26381","display":"Mr. Agustin437 Wyman904"}},{"item":{"reference":"Patient/61b2ab7a-22af-c77e-7bf0-6ab3280b22b9","display":"Mr. Alfredo17 Gaylord332"}},{"item":{"reference":"Patient/6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26","display":"Mr. Al123 Marquardt819"}}]},{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/302b80cd-3ef9-e663-db76-e6d397e26381","display":"Mr. Agustin437 Wyman904"}},{"item":{"reference":"Patient/61b2ab7a-22af-c77e-7bf0-6ab3280b22b9","display":"Mr. Alfredo17 Gaylord332"}},{"item":{"reference":"Patient/6be8e7d0-3519-5c6d-8ddb-8ccb55e7df26","display":"Mr. Al123 Marquardt819"}}]}],"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valuePeriod":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"}}],"name":"Measurement Period","use":"in","type":"Period"}},{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valueString":"ProductLine"}],"name":"Product Line","use":"in","type":"string"}}],"status":"complete","type":"subject-list","measure":"Measure/EXM127-9.2.000","period":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"},"group":[{"population":[{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"initial-population","display":"Initial Population"}],"text":"The initial population refers to all patients or events to be evaluated by a quality measure involving patients who share a common set of specified characterstics. All patients or events counted (for example, as numerator, as denominator) are drawn from the initial population."},"count":3,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"numerator","display":"Numerator"}],"text":"The upper portion of a fraction used to calculate a rate, proportion, or ratio. Also called the measure focus, it is the target process, condition, event, or outcome. Numerator criteria are the processes or outcomes expected for each patient, or event defined in the denominator. A numerator statement describes the clinical action that satisfies the conditions of the measure."},"count":3,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"denominator","display":"Denominator"}],"text":"The lower portion of a fraction used to calculate a rate, proportion, or ratio. The denominator can be the same as the initial population, or a subset of the initial population to further constrain the population for the purpose of the measure."},"count":3,"subjectResults":{"reference":"#"}}],"measureScore":{"value":1.0}}]}'
        actualResp = re.sub('\"id\":\"[a-z\-0-9]+\"','\"id\":\"\"',response)
        actualResp = re.sub('\"reference\":\"#[a-z\-0-9]+\"','\"reference\":\"#\"',actualResp)
        assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
    def test_evaluatePatientListMeasureUsingIdentifierPlusVersion(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-using-identifier-plus-version-request-data.json"
        measure = measurezip_path + "over60_and_had_colonoscopy_with_identifier_v1_1_1.zip"
        resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and Measure report returned'
        expectedResp = '{"resourceType":"MeasureReport","contained":[{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/a1637d9d-8de4-b8b8-be2e-94118c7f4d71","display":"Mrs. Agueda Wisozk"}},{"item":{"reference":"Patient/00ce7acb-5daa-3509-2e9f-211976bc70e1","display":"Ms. Kristie Schamberger"}}]},{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/00ce7acb-5daa-3509-2e9f-211976bc70e1","display":"Ms. Kristie Schamberger"}}]},{"resourceType":"List","id":"","entry":[{"item":{"reference":"Patient/a1637d9d-8de4-b8b8-be2e-94118c7f4d71","display":"Mrs. Agueda Wisozk"}},{"item":{"reference":"Patient/00ce7acb-5daa-3509-2e9f-211976bc70e1","display":"Ms. Kristie Schamberger"}}]}],"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valuePeriod":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"}}],"name":"Measurement Period","use":"in","type":"Period"}},{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valueString":"ProductLine"}],"name":"Product Line","use":"in","type":"string"}}],"status":"complete","type":"subject-list","measure":"Measure/Over60andHadColonoscopyFromVS-1.1.1","period":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"},"group":[{"population":[{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"initial-population"}]},"count":2,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"numerator"}]},"count":1,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"denominator"}]},"count":2,"subjectResults":{"reference":"#"}}],"measureScore":{"value":0.5}}]}'
        actualResp = re.sub('\"id\":\"[a-z\-0-9]+\"','\"id\":\"\"',response)
        actualResp = re.sub('\"reference\":\"#[a-z\-0-9]+\"','\"reference\":\"#\"',actualResp)
        assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
    def test_evaluatePatientListMeasureUsingEmptyPatientList(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-using-empty-patient-list-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 and a Measure report returned that contains no patient evaluation results and population counts that are all zeroes.'
        expectedResp = '{"resourceType":"MeasureReport","contained":[{"resourceType":"List","id":""},{"resourceType":"List","id":""},{"resourceType":"List","id":""}],"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valuePeriod":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"}}],"name":"Measurement Period","use":"in","type":"Period"}},{"url":"http://ibm.com/fhir/cdm/StructureDefinition/measure-parameter-value","valueParameterDefinition":{"extension":[{"url":"http://ibm.com/fhir/cdm/StructureDefinition/parameter-value","valueString":"ProductLine"}],"name":"Product Line","use":"in","type":"string"}}],"status":"complete","type":"subject-list","measure":"Measure/EXM127-9.2.000","period":{"start":"2020-06-25T00:00:00.000+00:00","end":"2021-06-24T23:59:59.999+00:00"},"group":[{"population":[{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"initial-population","display":"Initial Population"}],"text":"The initial population refers to all patients or events to be evaluated by a quality measure involving patients who share a common set of specified characterstics. All patients or events counted (for example, as numerator, as denominator) are drawn from the initial population."},"count":0,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"numerator","display":"Numerator"}],"text":"The upper portion of a fraction used to calculate a rate, proportion, or ratio. Also called the measure focus, it is the target process, condition, event, or outcome. Numerator criteria are the processes or outcomes expected for each patient, or event defined in the denominator. A numerator statement describes the clinical action that satisfies the conditions of the measure."},"count":0,"subjectResults":{"reference":"#"}},{"code":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/measure-population","code":"denominator","display":"Denominator"}],"text":"The lower portion of a fraction used to calculate a rate, proportion, or ratio. The denominator can be the same as the initial population, or a subset of the initial population to further constrain the population for the purpose of the measure."},"count":0,"subjectResults":{"reference":"#"}}]}]}'
        actualResp = re.sub('\"id\":\"[a-z\-0-9]+\"','\"id\":\"\"',response)
        actualResp = re.sub('\"reference\":\"#[a-z\-0-9]+\"','\"reference\":\"#\"',actualResp)
        assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
				
    def test_evaluatePatientListMeasureWithMissingValueSet(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-with-missing-valueset-ref-request-data.json"
        measure = measurezip_path + "EXM130-7.3.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in evaluatePatientListMeasureWithMissingValueSet is : " + result)
        assert '400' in result, 'Should contain 400 error due to IllegalArgumentException caught due to unresolved value set reference.'
		
    def test_evaluatePatientListMeasureWithInvalidAPIVersion1(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '2021/04/24'
        request_data = reqdata_path + "Eval-patient-list-measure-using-measureId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in evaluatePatientListMeasureWithInvalidAPIVersion1 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'

    def test_evaluatePatientListMeasureWithInvalidAPIVersion2(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = '04-24-2021'
        request_data = reqdata_path + "Eval-patient-list-measure-using-measureId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in evaluatePatientListMeasureWithInvalidAPIVersion2 is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Invalid version parameter value.'
	
    def test_evaluatePatientListMeasureWithWrongFHIREndpointPort(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-wrong-fhirendpoint-port-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in evaluatePatientListMeasureWithWrongFHIREndpointPort is : " + result)
        assert '500' in result, 'Should contain 500 Error message stating: Connect to fhir-internal.dev.svc:9444 failed: connect timed out.'

    def test_evaluatePatientListMeasureWithWrongFHIRUserPassword(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-wrong-fhiruser-password-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluateMeasureWithWrongFHIRUserPassword is : " + result)
        assert '400' in result, 'Should contain 400 Error: Bad request with the error message stating: HTTP 401 Unauthorized.'
	
    def test_evaluatePatientListMeasureWithInvalidTenantIdValue(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-with-invalid-tenantId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluatePatientListMeasureWithInvalidTenantIdValue is : " + result)
        assert '400' in result, 'Should contain a 400 Error due to the missing tenant configuration with the error message stating: Exception while communicating with FHIR.'
	
    def test_evaluatePatientListMeasureWithInvalidPatientId(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-with-invalid-patientId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluatePatientListMeasureWithInvalidPatientId is : " + result)
        assert '400' in result, 'Should contain 400 Error due to exception communicating with FHIR server as the specified Patient resource is not found on the server.'

    def test_evaluatePatientListMeasureWithBlankPatientId(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-with-blank-patientId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluatePatientListMeasureWithBlankPatientId is : " + result)
        assert '400' in result, 'Should contain 400 Error with the error message stating: The ID cannot be blank.'

    def test_evaluatePatientListMeasureWithInvalidMeasureId(self):
        measureEvaluationApi = MeasureEvaluationApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        request_data = reqdata_path + "Eval-patient-list-measure-with-invalid-measureId-request-data.json"
        measure = measurezip_path + "EXM127-9.2.000.zip"
        try:
            resp = measureEvaluationApi.evaluate_patient_list_measure(version, request_data, measure, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
            result = str(e)
            print("Exception in test_evaluatePatientListMeasureWithInvalidMeasureId is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Failed to resolve the specified Measure resource.'
