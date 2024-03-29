# coding: utf-8

"""
    IBM Cohort Engine

    Service to evaluate cohorts and measures  # noqa: E501

    OpenAPI spec version: 2.1.0 2022-02-18T21:50:45Z
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


import pprint
import re  # noqa: F401

import six

from swagger_client.configuration import Configuration


class EnhancedHealthCheckResults(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    """
    Attributes:
      swagger_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    swagger_types = {
        'data_server_connection_results': 'FhirServerConnectionStatusInfo',
        'terminology_server_connection_results': 'FhirServerConnectionStatusInfo'
    }

    attribute_map = {
        'data_server_connection_results': 'dataServerConnectionResults',
        'terminology_server_connection_results': 'terminologyServerConnectionResults'
    }

    def __init__(self, data_server_connection_results=None, terminology_server_connection_results=None, _configuration=None):  # noqa: E501
        """EnhancedHealthCheckResults - a model defined in Swagger"""  # noqa: E501
        if _configuration is None:
            _configuration = Configuration()
        self._configuration = _configuration

        self._data_server_connection_results = None
        self._terminology_server_connection_results = None
        self.discriminator = None

        self.data_server_connection_results = data_server_connection_results
        if terminology_server_connection_results is not None:
            self.terminology_server_connection_results = terminology_server_connection_results

    @property
    def data_server_connection_results(self):
        """Gets the data_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501


        :return: The data_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501
        :rtype: FhirServerConnectionStatusInfo
        """
        return self._data_server_connection_results

    @data_server_connection_results.setter
    def data_server_connection_results(self, data_server_connection_results):
        """Sets the data_server_connection_results of this EnhancedHealthCheckResults.


        :param data_server_connection_results: The data_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501
        :type: FhirServerConnectionStatusInfo
        """
        if self._configuration.client_side_validation and data_server_connection_results is None:
            raise ValueError("Invalid value for `data_server_connection_results`, must not be `None`")  # noqa: E501

        self._data_server_connection_results = data_server_connection_results

    @property
    def terminology_server_connection_results(self):
        """Gets the terminology_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501


        :return: The terminology_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501
        :rtype: FhirServerConnectionStatusInfo
        """
        return self._terminology_server_connection_results

    @terminology_server_connection_results.setter
    def terminology_server_connection_results(self, terminology_server_connection_results):
        """Sets the terminology_server_connection_results of this EnhancedHealthCheckResults.


        :param terminology_server_connection_results: The terminology_server_connection_results of this EnhancedHealthCheckResults.  # noqa: E501
        :type: FhirServerConnectionStatusInfo
        """

        self._terminology_server_connection_results = terminology_server_connection_results

    def to_dict(self):
        """Returns the model properties as a dict"""
        result = {}

        for attr, _ in six.iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value
        if issubclass(EnhancedHealthCheckResults, dict):
            for key, value in self.items():
                result[key] = value

        return result

    def to_str(self):
        """Returns the string representation of the model"""
        return pprint.pformat(self.to_dict())

    def __repr__(self):
        """For `print` and `pprint`"""
        return self.to_str()

    def __eq__(self, other):
        """Returns true if both objects are equal"""
        if not isinstance(other, EnhancedHealthCheckResults):
            return False

        return self.to_dict() == other.to_dict()

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        if not isinstance(other, EnhancedHealthCheckResults):
            return True

        return self.to_dict() != other.to_dict()
