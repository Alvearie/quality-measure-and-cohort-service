import sys
import os
from sys import path
from os import getcwd
path.append(getcwd() + "/tests/src/main/python/rest/tests/extract")
import swagger_client
import unittest
import json


class TestClass(unittest.TestCase):
    #Connection Details
    configuration = swagger_client.Configuration()
    configuration.host = os.environ['REST_SERVER_ENDPOINT']
    configuration.verify_ssl = False
