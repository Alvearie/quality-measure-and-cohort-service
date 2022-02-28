# swagger_client.ValueSetApi

All URIs are relative to *https://localhost/services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**create_value_set**](ValueSetApi.md#create_value_set) | **POST** /v1/valueset | Insert a new value set to the fhir server or, if it already exists, update it in place


# **create_value_set**
> create_value_set(version, fhir_data_server_config, value_set, update_if_exists=update_if_exists, custom_code_system=custom_code_system)

Insert a new value set to the fhir server or, if it already exists, update it in place

Uploads a value set described by the given xslx file

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.ValueSetApi()
version = '2022-02-18' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2022-02-18)
fhir_data_server_config = '/path/to/file.txt' # file | A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/fhir-server-config.md for more details.  Example Contents:   <pre>{     \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",     \"endpoint\": \"https://fhir-internal.dev:9443/fhir-server/api/v4\",     \"user\": \"fhiruser\",     \"password\": \"replaceWithfhiruserPassword\",     \"logInfo\": [         \"ALL\"     ],     \"tenantId\": \"default\" }</pre>
value_set = '/path/to/file.txt' # file | Spreadsheet containing the Value Set definition.
update_if_exists = false # bool | The parameter that, if true, will force updates of value sets if the value set already exists (optional) (default to false)
custom_code_system = '/path/to/file.txt' # file | A custom mapping of code systems to urls (optional)

try:
    # Insert a new value set to the fhir server or, if it already exists, update it in place
    api_instance.create_value_set(version, fhir_data_server_config, value_set, update_if_exists=update_if_exists, custom_code_system=custom_code_system)
except ApiException as e:
    print("Exception when calling ValueSetApi->create_value_set: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2022-02-18]
 **fhir_data_server_config** | **file**| A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/fhir-server-config.md for more details.  Example Contents:   &lt;pre&gt;{     \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,     \&quot;endpoint\&quot;: \&quot;https://fhir-internal.dev:9443/fhir-server/api/v4\&quot;,     \&quot;user\&quot;: \&quot;fhiruser\&quot;,     \&quot;password\&quot;: \&quot;replaceWithfhiruserPassword\&quot;,     \&quot;logInfo\&quot;: [         \&quot;ALL\&quot;     ],     \&quot;tenantId\&quot;: \&quot;default\&quot; }&lt;/pre&gt; | 
 **value_set** | **file**| Spreadsheet containing the Value Set definition. | 
 **update_if_exists** | **bool**| The parameter that, if true, will force updates of value sets if the value set already exists | [optional] [default to false]
 **custom_code_system** | **file**| A custom mapping of code systems to urls | [optional] 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

