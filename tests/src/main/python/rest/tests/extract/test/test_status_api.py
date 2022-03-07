# coding: utf-8

"""
    IBM Cohort Engine

    Service to evaluate cohorts and measures  # noqa: E501

    OpenAPI spec version: 2.1.0 2022-02-18T21:50:45Z
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import unittest

import swagger_client
from swagger_client.api.status_api import StatusApi  # noqa: E501
from swagger_client.rest import ApiException


class TestStatusApi(unittest.TestCase):
    """StatusApi unit test stubs"""

    def setUp(self):
        self.api = swagger_client.api.status_api.StatusApi()  # noqa: E501

    def tearDown(self):
        pass

    def test_get_health_check_status(self):
        """Test case for get_health_check_status

        Determine if service is running correctly  # noqa: E501
        """
        pass

    def test_get_service_status(self):
        """Test case for get_service_status

        Get status of service  # noqa: E501
        """
        pass

    def test_health_check_enhanced(self):
        """Test case for health_check_enhanced

        Get the status of the cohorting service and dependent downstream services  # noqa: E501
        """
        pass


if __name__ == '__main__':
    unittest.main()
