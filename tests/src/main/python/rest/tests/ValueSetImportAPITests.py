from __future__ import absolute_import
import sys
import os
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.value_set_api import ValueSetApi
from swagger_client.rest import ApiException

# Set global variables in the file that can be accessed by all tests within the class
config_path = "/bzt-configs/tests/src/main/resources/config/"
valueset_path = "/bzt-configs/tests/src/main/resources/valuesets/"

class ValueSetImportAPITests(TestClass):

    def test_valueSetImportOfExistingVSWithUpdateFlagFalse(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=False, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportOfExistingVSWithUpdateFlagFalse is : " + result)
        assert '409' in result, 'Should contain 409 error with message stating: Value Set already exists! Rerun with updateIfExists set to true!'
       
    
    def test_valueSetImportOfExistingVSWithUpdateFlagTrue(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '201' in str(status), 'Should contain 201 - valueSetId:178f0e4a575-eb06dd03-f912-4c68-9dcd-812e603973f0'
        expectedResp = '{"valueSetId":"178f0e4a575-eb06dd03-f912-4c68-9dcd-812e603973f0"}'
        assert expectedResp in response, 'Response should contain ' + expectedResp

    def test_valueSetImportOfExistingVSWithCustomCodeSystemMapAndUpdateFlagFalse(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1008.xlsx"
        custom_code_system = "/bzt-configs/tests/src/main/resources/valuesets/CustomCodeSystem.txt"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=False, custom_code_system=custom_code_system, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportOfExistingVSWithCustomCodeSystemMapAndUpdateFlagFalse is : " + result)
        assert '409' in result, 'Should contain 409 error with message stating: Value Set already exists! Rerun with updateIfExists set to true!'

    def test_valueSetImportOfExistingVSWithCustomCodeSystemMapAndUpdateFlagTrue(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = "/bzt-configs/tests/src/main/resources/valuesets/2.16.840.1.113883.3.464.1003.101.12.1008.xlsx"
        custom_code_system = valueset_path + "CustomCodeSystem.txt"
        resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, custom_code_system=custom_code_system, _preload_content=False)
        response = resp.read()
        response = response.decode('utf-8')
        print("response is : " + str(response))
        status = resp.status
        print("status from response is : " + str(status))
        assert '201' in str(status), 'Should contain 201 - valueSetId:178f0e4fe84-f8b3f173-c976-4bff-8a60-eec3bb8e74be'
        expectedResp = '{"valueSetId":"178f0e4fe84-f8b3f173-c976-4bff-8a60-eec3bb8e74be"}'
        assert expectedResp in response, 'Response should contain ' + expectedResp

    def test_valueSetImportWithWrongCustomCodeSystemMap(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1008.xlsx"
        custom_code_system = valueset_path + "WrongCustomCodeSystem.txt"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, custom_code_system=custom_code_system, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithWrongCustomCodeSytemMap is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Unknown CodeSystem name: CUSTOM_SNOMED'
    
    def test_valueSetImportWithNoCustomCodeSystemMap(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1008.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithNoCustomCodeSystemMap is : " + result)
        assert '400' in result, 'Should contain 400 error with message stating: Unknown CodeSystem name: CUSTOM_SNOMED'

    def test_valueSetImportWithInvalidValueSetSpreadsheetFormat(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = date.today()
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.csv"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithInvalidValueSetSpreadsheetFormat is : " + result)
        assert '500' in result, 'Should contain 500 error with a message stating: No valid entries or contents found, this is not a valid OOXML (Office Open XML) file.'

    def test_valueSetImportWithInvalidAPIVersionValue1(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = '2021/04/21'
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetWithInvalidAPIVersionValue1 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid Api version value specified.'

    def test_valueSetImportWithInvalidAPIVersionValue2(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = '04-21-2021'
        fhir_data_server_config = os.environ['FHIR_SERVER_DETAILS_JSON']
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithInvalidAPIVersionValue2 is : " + result)
        assert '400' in result, 'Should contain 400 error due to invalid Api version value specified.'

    def test_valueSetImportWithWrongFHIREndpointPort(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = '04-21-2021'
        fhir_data_server_config = config_path + "fhirconfig-badendpoint-port.json"
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithWrongFHIREndpointPort is : " + result)
        assert '400' in result, 'Should contain 400 error with a message stating: Connect to fhir-internal.dev.svc:9444 failed.'

    def test_valueSetImportWithWrongFHIRUserPassword(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = '04-21-2021'
        fhir_data_server_config = config_path + "fhirconfig-knowledge-tenant-wrong-password.json"
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithWrongFHIRUserPassword is : " + result)
        assert '400' in result, 'Should contain 400 error with a message stating: HTTP 401 Unauthorized.'

    def test_valueSetImportWithInvalidTenantIdValue(self):
        valueSetApi = ValueSetApi(swagger_client.ApiClient(self.configuration))
        version = '04-21-2021'
        fhir_data_server_config = config_path + "fhirconfig-invalid-tenant.json"
        value_set = valueset_path + "2.16.840.1.113883.3.464.1003.101.12.1001.xlsx"
        try:
            resp = valueSetApi.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=True, _preload_content=False)
            result = resp.read()
        except ApiException as e:
            print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
            result = str(e)
            print("Exception in test_valueSetImportWithInvalidTenantIdValue is : " + result)
        assert '400' in result, 'Should contain 400 error with a message stating: FHIRPersistenceException: Unexpected exception while creating JDBC persistence layer.'

    