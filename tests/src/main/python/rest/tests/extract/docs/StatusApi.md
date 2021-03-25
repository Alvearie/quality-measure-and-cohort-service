# swagger_client.StatusApi

All URIs are relative to */services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_health_check_status**](StatusApi.md#get_health_check_status) | **GET** /v1/status/health_check | Determine if service is running correctly
[**get_service_status**](StatusApi.md#get_service_status) | **GET** /v1/status | Get status of service

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

