# swagger_client.FHIRMeasuresApi

All URIs are relative to *https://localhost/services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_measure_parameters**](FHIRMeasuresApi.md#get_measure_parameters) | **POST** /v1/fhir/measure/identifier/{measure_identifier_value}/parameters | Get measure parameters
[**get_measure_parameters_by_id**](FHIRMeasuresApi.md#get_measure_parameters_by_id) | **POST** /v1/fhir/measure/{measure_id}/parameters | Get measure parameters by id


# **get_measure_parameters**
> MeasureParameterInfoList get_measure_parameters(version, measure_identifier_value, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version)

Get measure parameters

Retrieves the parameter information for libraries linked to by a measure

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.FHIRMeasuresApi()
version = '2021-04-26' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-04-26)
measure_identifier_value = 'measure_identifier_value_example' # str | Used to identify the FHIR measure resource you would like the parameter information for using the Measure.Identifier.Value field.
fhir_data_server_config = '/path/to/file.txt' # file | A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   <pre>{     \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",     \"endpoint\": \"https://fhir-internal.dev:9443/fhir-server/api/v4\",     \"user\": \"fhiruser\",     \"password\": \"replaceWithfhiruserPassword\",     \"logInfo\": [         \"ALL\"     ],     \"tenantId\": \"default\" }</pre>
measure_identifier_system = 'measure_identifier_system_example' # str | The system name used to provide a namespace for the measure identifier values. For example, if using social security numbers for the identifier values, one would use http://hl7.org/fhir/sid/us-ssn as the system value. (optional)
measure_version = 'measure_version_example' # str |  The version of the measure to retrieve as represented by the FHIR resource Measure.version field. If a value is not provided, the underlying code will atempt to resolve the most recent version assuming a <Major>.<Minor>.<Patch> format (ie if versions 1.0.0 and 2.0.0 both exist, the code will return the 2.0.0 version) (optional)

try:
    # Get measure parameters
    api_response = api_instance.get_measure_parameters(version, measure_identifier_value, fhir_data_server_config, measure_identifier_system=measure_identifier_system, measure_version=measure_version)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FHIRMeasuresApi->get_measure_parameters: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-04-26]
 **measure_identifier_value** | **str**| Used to identify the FHIR measure resource you would like the parameter information for using the Measure.Identifier.Value field. | 
 **fhir_data_server_config** | **file**| A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   &lt;pre&gt;{     \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,     \&quot;endpoint\&quot;: \&quot;https://fhir-internal.dev:9443/fhir-server/api/v4\&quot;,     \&quot;user\&quot;: \&quot;fhiruser\&quot;,     \&quot;password\&quot;: \&quot;replaceWithfhiruserPassword\&quot;,     \&quot;logInfo\&quot;: [         \&quot;ALL\&quot;     ],     \&quot;tenantId\&quot;: \&quot;default\&quot; }&lt;/pre&gt; | 
 **measure_identifier_system** | **str**| The system name used to provide a namespace for the measure identifier values. For example, if using social security numbers for the identifier values, one would use http://hl7.org/fhir/sid/us-ssn as the system value. | [optional] 
 **measure_version** | **str**|  The version of the measure to retrieve as represented by the FHIR resource Measure.version field. If a value is not provided, the underlying code will atempt to resolve the most recent version assuming a &lt;Major&gt;.&lt;Minor&gt;.&lt;Patch&gt; format (ie if versions 1.0.0 and 2.0.0 both exist, the code will return the 2.0.0 version) | [optional] 

### Return type

[**MeasureParameterInfoList**](MeasureParameterInfoList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_measure_parameters_by_id**
> MeasureParameterInfoList get_measure_parameters_by_id(version, measure_id, fhir_data_server_config)

Get measure parameters by id

Retrieves the parameter information for libraries linked to by a measure

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.FHIRMeasuresApi()
version = '2021-04-26' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-04-26)
measure_id = 'measure_id_example' # str | FHIR measure resource id for the measure you would like the parameter information for using the Measure.id field.
fhir_data_server_config = '/path/to/file.txt' # file | A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   <pre>{     \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",     \"endpoint\": \"https://fhir-internal.dev:9443/fhir-server/api/v4\",     \"user\": \"fhiruser\",     \"password\": \"replaceWithfhiruserPassword\",     \"logInfo\": [         \"ALL\"     ],     \"tenantId\": \"default\" }</pre>

try:
    # Get measure parameters by id
    api_response = api_instance.get_measure_parameters_by_id(version, measure_id, fhir_data_server_config)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FHIRMeasuresApi->get_measure_parameters_by_id: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-04-26]
 **measure_id** | **str**| FHIR measure resource id for the measure you would like the parameter information for using the Measure.id field. | 
 **fhir_data_server_config** | **file**| A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   &lt;pre&gt;{     \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,     \&quot;endpoint\&quot;: \&quot;https://fhir-internal.dev:9443/fhir-server/api/v4\&quot;,     \&quot;user\&quot;: \&quot;fhiruser\&quot;,     \&quot;password\&quot;: \&quot;replaceWithfhiruserPassword\&quot;,     \&quot;logInfo\&quot;: [         \&quot;ALL\&quot;     ],     \&quot;tenantId\&quot;: \&quot;default\&quot; }&lt;/pre&gt; | 

### Return type

[**MeasureParameterInfoList**](MeasureParameterInfoList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

