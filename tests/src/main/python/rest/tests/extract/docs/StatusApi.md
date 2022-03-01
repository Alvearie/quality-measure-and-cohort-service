# swagger_client.StatusApi

All URIs are relative to *https://localhost/services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_health_check_status**](StatusApi.md#get_health_check_status) | **GET** /v1/status/health_check | Determine if service is running correctly
[**get_service_status**](StatusApi.md#get_service_status) | **GET** /v1/status | Get status of service
[**health_check_enhanced**](StatusApi.md#health_check_enhanced) | **POST** /v1/status/health_check_enhanced | Get the status of the cohorting service and dependent downstream services


# **get_health_check_status**
> ServiceStatus get_health_check_status(format=format)

Determine if service is running correctly

This resource differs from /status in that it will will always return a 500 error if the service state is not OK.  This makes it simpler for service front ends (such as Datapower) to detect a failed service.

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.StatusApi()
format = 'format_example' # str | Override response format (optional)

try:
    # Determine if service is running correctly
    api_response = api_instance.get_health_check_status(format=format)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling StatusApi->get_health_check_status: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **format** | **str**| Override response format | [optional] 

### Return type

[**ServiceStatus**](ServiceStatus.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_service_status**
> ServiceStatus get_service_status(format=format, liveness_check=liveness_check)

Get status of service



### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.StatusApi()
format = 'format_example' # str | Override response format (optional)
liveness_check = 'false' # str | Perform a shallow liveness check (optional) (default to false)

try:
    # Get status of service
    api_response = api_instance.get_service_status(format=format, liveness_check=liveness_check)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling StatusApi->get_service_status: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **format** | **str**| Override response format | [optional] 
 **liveness_check** | **str**| Perform a shallow liveness check | [optional] [default to false]

### Return type

[**ServiceStatus**](ServiceStatus.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/xml

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **health_check_enhanced**
> EnhancedHealthCheckResults health_check_enhanced(version, fhir_server_connection_config)

Get the status of the cohorting service and dependent downstream services

Checks the status of the cohorting service and any downstream services used by the cohorting service

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.StatusApi()
version = '2022-02-18' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2022-02-18)
fhir_server_connection_config = '/path/to/file.txt' # file | A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/fhir-server-config.md for more details.  <p>Example Contents:   <pre>{     \"dataServerConfig\": {         \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",         \"endpoint\": \"ENDPOINT\",         \"user\": \"USER\",         \"password\": \"PASSWORD\",         \"logInfo\": [             \"REQUEST_SUMMARY\",             \"RESPONSE_SUMMARY\"         ],         \"tenantId\": \"default\"     },     \"terminologyServerConfig\": {         \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",         \"endpoint\": \"ENDPOINT\",         \"user\": \"USER\",         \"password\": \"PASSWORD\",         \"logInfo\": [             \"REQUEST_SUMMARY\",             \"RESPONSE_SUMMARY\"         ],         \"tenantId\": \"default\"     } }</pre></p>

try:
    # Get the status of the cohorting service and dependent downstream services
    api_response = api_instance.health_check_enhanced(version, fhir_server_connection_config)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling StatusApi->health_check_enhanced: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2022-02-18]
 **fhir_server_connection_config** | **file**| A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/fhir-server-config.md for more details.  &lt;p&gt;Example Contents:   &lt;pre&gt;{     \&quot;dataServerConfig\&quot;: {         \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,         \&quot;endpoint\&quot;: \&quot;ENDPOINT\&quot;,         \&quot;user\&quot;: \&quot;USER\&quot;,         \&quot;password\&quot;: \&quot;PASSWORD\&quot;,         \&quot;logInfo\&quot;: [             \&quot;REQUEST_SUMMARY\&quot;,             \&quot;RESPONSE_SUMMARY\&quot;         ],         \&quot;tenantId\&quot;: \&quot;default\&quot;     },     \&quot;terminologyServerConfig\&quot;: {         \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,         \&quot;endpoint\&quot;: \&quot;ENDPOINT\&quot;,         \&quot;user\&quot;: \&quot;USER\&quot;,         \&quot;password\&quot;: \&quot;PASSWORD\&quot;,         \&quot;logInfo\&quot;: [             \&quot;REQUEST_SUMMARY\&quot;,             \&quot;RESPONSE_SUMMARY\&quot;         ],         \&quot;tenantId\&quot;: \&quot;default\&quot;     } }&lt;/pre&gt;&lt;/p&gt; | 

### Return type

[**EnhancedHealthCheckResults**](EnhancedHealthCheckResults.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

