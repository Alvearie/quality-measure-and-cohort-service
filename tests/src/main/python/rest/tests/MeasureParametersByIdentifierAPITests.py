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

class MeasureParametersByIdentifierAPITests(TestClass):

    def test_getMeasureParametersByIdentifierWithNoVersion(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        measure_identifier = "999"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 - Successful Operation.'
        expectedResp = '{"parameterInfoList":[{"defaultValue":"42","max":"1","min":0,"name":"aName","type":"ParameterDefinition","use":"In"}]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp
    
    def test_getMeasureParametersByIdentifierWithVersion(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        measure_identifier = "999"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        status = resp.status
        print("status from response is : " + str(status))
        assert '200' in str(status), 'Should contain 200 - Successful Operation.'
        expectedResp = '{"parameterInfoList":[{"defaultValue":"42","max":"1","min":0,"name":"aName","type":"ParameterDefinition","use":"In"}]}'
        assert expectedResp in response, 'Response should contain ' + expectedResp

    def test_getMeasureParametersByIdentifierWithInvalidAPIVersionValue1(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021/04/12"
        measure_identifier = "999"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithInvalidAPIVersionValue1 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid api version parameter value specified.'

    def test_getMeasureParametersByIdentifierWithInvalidAPIVersionValue2(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "04-12-2021"
        measure_identifier = "999"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithInvalidAPIVersionValue2 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid api version parameter value specified.'  

    def test_getMeasureParametersByIdentifierWithInvalidIdentifierValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "900"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithInvalidIdentifierValue is : " + result)
        assert '400' in result, 'Should contain 400 error due to Measure resource with specified measure identifier not found on FHIR server.'

    def test_getMeasureParametersByIdentifierWithInvalidVersion(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "999"
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "4.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithInvalidVersion is : " + result)
        assert '400' in result, 'Should contain 400 error due to Measure resource not found for the specified measure version.' 

    def test_getMeasureParametersByIdentifierWithWrongTenantIdValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "999"
        fhir_data_server_config = config_path + "fhirconfig-testfvt-tenant.json"
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithWrongTenantIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error due to Measure resource not found on the specified tenant on FHIR server.'

    def test_getMeasureParametersByIdentifierWithInvalidTenantIdValue(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "999"
        fhir_data_server_config = config_path + "fhirconfig-invalid-tenant.json"
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithInvalidTenantIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error due to an exception while creating JDBC persistence layer.'

    def test_getMeasureParametersByIdentifierWithWrongFHIREndpointPort(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "999"
        fhir_data_server_config = config_path + "fhirconfig-badendpoint-port.json"
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithWrongFHIREndpointPort is : " + result)
        assert '400' in result, 'Should contain 400 error message stating: Connect to fhir-internal.dev.svc:9444 failed.'

    def test_getMeasureParametersByIdentifierWithWrongFHIRUserPassword(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        version = "2021-04-12"
        measure_identifier = "999"
        fhir_data_server_config = config_path + "fhirconfig-knowledge-tenant-wrong-password.json"
        measure_identifier_system = "http://fakesystem.org"
        measure_version = "3.0.0"
        try:
            resp = fhirMeasuresApi.get_measure_parameters(version, measure_identifier, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version, _preload_content=False)
            result = resp.read()
        except ApiException as e: 
            print("Exception when calling FHIRMeasureApi->get_measure_parameters: %s\n" % e)
            result = str(e)
            print("Exception in test_getMeasureParametersByIdentifierWithWrongFHIRUserPassword is : " + result)
        assert '400' in result, 'Should contain 400 error message stating: HTTP 401 Unauthorized.'