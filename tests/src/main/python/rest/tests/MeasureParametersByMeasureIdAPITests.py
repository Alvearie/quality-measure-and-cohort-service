from __future__ import absolute_import
import sys
import os
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.fhir_measures_api import FHIRMeasuresApi
from swagger_client.rest import ApiException

# Set global variable in file that can be accessed by all tests within the class
config_path = "/bzt-configs/tests/src/main/resources/config/"

class MeasureParametersByMeasureIdAPITests(TestClass):

    def test_getMeasureParametersById(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + response)
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 - Successful Operation.'
        expectedResp = '{"parameterInfoList":[{"defaultValue":"42","max":"1","min":0,"name":"aName","type":"ParameterDefinition","use":"In"}]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp
    
    def test_getMeasureParametersByIdWithInvalidAPIVersionValue1(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = '2021/04/12'
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdWithInvalidAPIVersionValue1 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid api version parameter value specified.'

    def test_getMeasureParametersByIdWithInvalidAPIVersionValue2(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = '04-12-2021'
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdWithInvalidAPIVersionValue2 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid api version parameter value specified.'

    def test_getMeasureParametersByIdWithInvalidMeasureIdValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d30'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception received in test_getMeasureParametersByIdWithInvalidMeasureIdValue is: " + result)
        assert '400' in result, 'Should contain 400 error due to Measure resource with specified Measure Id not found on FHIR server.'

    def test_getMeasureParametersByIdWithWrongTenantIdValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = date.today()
        fhir_data_server_config = config_path + "fhirconfig-testfvt-tenant.json"
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception received in test_getMeasureParametersByIdWithWrongTenantIdValue is: " + result)
        assert '400' in result, 'Should contain 400 error due to Measure resource not found on specified tenant on FHIR server.'

    def test_getMeasureParametersByIdWithInvalidTenantIdValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = date.today()
        fhir_data_server_config = config_path + "fhirconfig-invalid-tenant.json"
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception received in test_getMeasureParametersByIdWithInvalidTenantIdValue is: " + result)
        assert '400' in result, 'Should contain 400 error due to an exception while creating JDBC persistence layer.'

    
    def test_getMeasureParametersByIdWithWrongFHIREndpointPort(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = date.today()
        fhir_data_server_config = config_path + "fhirconfig-badendpoint-port.json"
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception received in test_getMeasureParametersByIdWithWrongFHIREndpointPort is: " + result)
        assert '400' in result, 'Should contain 400 error message stating: Connect to fhir-internal.dev.svc:9444 failed.'

    def test_getMeasureParametersByIdWithWrongFHIRUserPassword(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = date.today()
        fhir_data_server_config = config_path + "fhirconfig-knowledge-tenant-wrong-password.json"
        measure_id = '178378911bd-e8a3413b-27da-45b3-bc28-d079f0ef0d38'
        try:
            resp = fhirMeasuresApi.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters_by_id: %s\n" % e)
            result = str(e)
            print("Exception received in test_getMeasureParametersByIdWithWrongFHIRUserPassword is: " + result)
        assert '400' in result, 'Should contain 400 error message stating: HTTP 401 Unauthorized.'