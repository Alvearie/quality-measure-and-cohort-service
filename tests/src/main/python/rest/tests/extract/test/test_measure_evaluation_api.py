# coding: utf-8

"""
    IBM Cohort Engine

    Service to evaluate cohorts and measures  # noqa: E501

    OpenAPI spec version: 0.0.1 2021-04-26T16:43:57Z
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import unittest

import swagger_client
from swagger_client.api.measure_evaluation_api import MeasureEvaluationApi  # noqa: E501
from swagger_client.rest import ApiException


class TestMeasureEvaluationApi(unittest.TestCase):
    """MeasureEvaluationApi unit test stubs"""

    def setUp(self):
        self.api = swagger_client.api.measure_evaluation_api.MeasureEvaluationApi()  # noqa: E501

    def tearDown(self):
        pass

    def test_evaluate_measure(self):
        """Test case for evaluate_measure

        Evaluates a measure bundle for a single patient  # noqa: E501
        """
        pass


if __name__ == '__main__':
    unittest.main()