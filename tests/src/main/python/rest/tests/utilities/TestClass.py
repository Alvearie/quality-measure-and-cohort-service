import sys
import os
from sys import path
from os import getcwd
path.append(getcwd() + "/../../src/main/python/rest/tests/extract")
import swagger_client
import unittest
import json


class TestClass(unittest.TestCase):
    #Connection Details
    configuration = swagger_client.Configuration()
    #Check if we should read from a json file or environment variable.
    if os.environ['FHIR_JSON']=='False':
        #Load from Environment.
        configuration.username = os.environ['FHIRUSER']
        configuration.password = os.environ['FHIRPASS']
        fhir_endpoint = 'https://fhir-internal.dev:9443/fhir-server/api/v4'
        fhir_tenant = 'Default'
    else:
        #Load from JSON file and set.
        #config/local-ibm-fhir.json <- whats the real one? See create_pod shell script.
        with open(os.environ['FHIR_JSON']) as f:
            data = json.load(f)
            configuration.username = data['user']
            configuration.password = data['password']
            fhir_endpoint = data['endpoint']
            fhir_tenant = data['tenantId']

    configuration.host = os.environ['HOST']
    
    if os.environ['VERIFY_SSL'] == 'False':
        configuration.verify_ssl = False
    else:
        configuraiton.verify_ssl = True
    #Common Values
    