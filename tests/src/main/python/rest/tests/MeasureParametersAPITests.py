import sys
import os
from sys import path
from os import getcwd
from utilities.TestClass import TestClass
path.append(getcwd() + "/../../src/main/python/rest/tests/extract")
import swagger_client
from swagger_client.api.fhir_measures_api import FHIRMeasuresApi

class MeasureParametersAPITest(TestClass):

    def test_simpleErrorCase(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        try:
            resp = fhirMeasuresApi.get_measure_parameters('2021-03-15',self.fhir_endpoint,'default','badMeasureID')
            result = resp.read()
        except Exception as e: 
            print(str(e))
            result = str(e)
        assert '500' in result, 'Should contain 500 error.'

    def test_simpleCase(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))

        resp = fhirMeasuresApi.get_measure_parameters('2021-03-15',self.fhir_endpoint,'default','999',_preload_content = False)
        response = resp.read()
        response = response.decode('utf-8')
        status = resp.status
        result = 'STATUS: ' + str(status) + ' RESPONSE: ' + response
        assert '200' in result, 'Should contain 200.'
    
    def test_invalidFHIREndpoint(self):
        fhirMeasuresApi = FHIRMeasuresApi(swagger_client.ApiClient(self.configuration))
        result = None
        try:
            resp = fhirMeasuresApi.get_measure_parameters('2021-03-15','badURL','default','badMeasureID')
            result = resp.read()
        except Exception as e: 
            print(str(e))
            result = str(e)
        assert '400' in result, 'Should contain 400 error.'