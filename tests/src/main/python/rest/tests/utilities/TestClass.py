import sys
import os
from sys import path
from os import getcwd
path.append(getcwd() + "/../../src/main/python/rest/tests/extract")
import swagger_client
import unittest

class TestClass(unittest.TestCase):
    configuration = swagger_client.Configuration()
    configuration.username = os.environ['FHIRUSER']
    configuration.password = os.environ['FHIRPASS']
    configuration.host = os.environ['HOST']
    configuration.verify_ssl = False