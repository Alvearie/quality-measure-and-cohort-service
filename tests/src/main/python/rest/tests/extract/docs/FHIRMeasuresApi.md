# swagger_client.FHIRMeasuresApi

All URIs are relative to */services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_measure_parameters**](FHIRMeasuresApi.md#get_measure_parameters) | **GET** /v1/fhir/measure/identifier/{measure_identifier_value}/parameters | Get measure parameters
[**get_measure_parameters_by_id**](FHIRMeasuresApi.md#get_measure_parameters_by_id) | **GET** /v1/fhir/measure/{measure_id}/parameters | Get measure parameters by id

# **get_measure_parameters**
> list[MeasureParameterInfoList] get_measure_parameters(version, fhir_server_rest_endpoint, fhir_server_tenant_id, measure_identifier_value, measure_identifier_system=measure_identifier_system, measure_version=measure_version, fhir_server_tenant_id_header=fhir_server_tenant_id_header, fhir_data_source_id_header=fhir_data_source_id_header, fhir_data_source_id=fhir_data_source_id)

Get measure parameters

Retrieves the parameter information for libraries linked to by a measure

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint
# Configure HTTP basic authorization: BasicAuth
configuration = swagger_client.Configuration()
configuration.username = 'YOUR_USERNAME'
configuration.password = 'YOUR_PASSWORD'

# create an instance of the API class
api_instance = swagger_client.FHIRMeasuresApi(swagger_client.ApiClient(configuration))
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)
fhir_server_rest_endpoint = 'https://fhir-internal.dev:9443/fhir-server/api/v4' # str | The REST endpoint of the FHIR server the measure/libraries are stored in. For example: https://localhost:9443/fhir-server/api/v4 (default to https://fhir-internal.dev:9443/fhir-server/api/v4)
fhir_server_tenant_id = 'default' # str | The id of the tenant used to store the measure/library information in the FHIR server.  (default to default)
measure_identifier_value = 'measure_identifier_value_example' # str | Used to identify the FHIR measure resource you would like the parameter information for using the Measure.Identifier.Value field.
measure_identifier_system = 'measure_identifier_system_example' # str | The system name used to provide a namespace for the measure identifier values. For example, if using social security numbers for the identifier values, one would use http://hl7.org/fhir/sid/us-ssn as the system value. (optional)
measure_version = 'measure_version_example' # str |  The version of the measure to retrieve as represented by the FHIR resource Measure.version field. If a value is not provided, the underlying code will atempt to resolve the most recent version assuming a <Major>.<Minor>.<Patch> format (ie if versions 1.0.0 and 2.0.0 both exist, the code will return the 2.0.0 version) (optional)
fhir_server_tenant_id_header = 'X-FHIR-TENANT-ID' # str | IBM FHIR Server uses HTTP headers to control which underlying tenant contains the data being retrieved. The default header name used to identify the tenant can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-TENANT-ID) is used. (optional) (default to X-FHIR-TENANT-ID)
fhir_data_source_id_header = 'X-FHIR-DSID' # str | IBM FHIR Server uses HTTP headers to control which underlying datasource contains the data being retrieved. The default header can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-DSID) is used. (optional) (default to X-FHIR-DSID)
fhir_data_source_id = 'fhir_data_source_id_example' # str | The id of the underlying datasource used by the FHIR server to contain the data. (optional)

try:
    # Get measure parameters
    api_response = api_instance.get_measure_parameters(version, fhir_server_rest_endpoint, fhir_server_tenant_id, measure_identifier_value, measure_identifier_system=measure_identifier_system, measure_version=measure_version, fhir_server_tenant_id_header=fhir_server_tenant_id_header, fhir_data_source_id_header=fhir_data_source_id_header, fhir_data_source_id=fhir_data_source_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FHIRMeasuresApi->get_measure_parameters: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]
 **fhir_server_rest_endpoint** | **str**| The REST endpoint of the FHIR server the measure/libraries are stored in. For example: https://localhost:9443/fhir-server/api/v4 | [default to https://fhir-internal.dev:9443/fhir-server/api/v4]
 **fhir_server_tenant_id** | **str**| The id of the tenant used to store the measure/library information in the FHIR server.  | [default to default]
 **measure_identifier_value** | **str**| Used to identify the FHIR measure resource you would like the parameter information for using the Measure.Identifier.Value field. | 
 **measure_identifier_system** | **str**| The system name used to provide a namespace for the measure identifier values. For example, if using social security numbers for the identifier values, one would use http://hl7.org/fhir/sid/us-ssn as the system value. | [optional] 
 **measure_version** | **str**|  The version of the measure to retrieve as represented by the FHIR resource Measure.version field. If a value is not provided, the underlying code will atempt to resolve the most recent version assuming a &lt;Major&gt;.&lt;Minor&gt;.&lt;Patch&gt; format (ie if versions 1.0.0 and 2.0.0 both exist, the code will return the 2.0.0 version) | [optional] 
 **fhir_server_tenant_id_header** | **str**| IBM FHIR Server uses HTTP headers to control which underlying tenant contains the data being retrieved. The default header name used to identify the tenant can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-TENANT-ID) is used. | [optional] [default to X-FHIR-TENANT-ID]
 **fhir_data_source_id_header** | **str**| IBM FHIR Server uses HTTP headers to control which underlying datasource contains the data being retrieved. The default header can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-DSID) is used. | [optional] [default to X-FHIR-DSID]
 **fhir_data_source_id** | **str**| The id of the underlying datasource used by the FHIR server to contain the data. | [optional] 

### Return type

[**list[MeasureParameterInfoList]**](MeasureParameterInfoList.md)

### Authorization

[BasicAuth](../README.md#BasicAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_measure_parameters_by_id**
> list[MeasureParameterInfoList] get_measure_parameters_by_id(version, fhir_server_rest_endpoint, fhir_server_tenant_id, measure_id, fhir_server_tenant_id_header=fhir_server_tenant_id_header, fhir_data_source_id_header=fhir_data_source_id_header, fhir_data_source_id=fhir_data_source_id)

Get measure parameters by id

Retrieves the parameter information for libraries linked to by a measure

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint
# Configure HTTP basic authorization: BasicAuth
configuration = swagger_client.Configuration()
configuration.username = 'YOUR_USERNAME'
configuration.password = 'YOUR_PASSWORD'

# create an instance of the API class
api_instance = swagger_client.FHIRMeasuresApi(swagger_client.ApiClient(configuration))
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)
fhir_server_rest_endpoint = 'https://fhir-internal.dev:9443/fhir-server/api/v4' # str | The REST endpoint of the FHIR server the measure/libraries are stored in. For example: https://localhost:9443/fhir-server/api/v4 (default to https://fhir-internal.dev:9443/fhir-server/api/v4)
fhir_server_tenant_id = 'default' # str | The id of the tenant used to store the measure/library information in the FHIR server.  (default to default)
measure_id = 'measure_id_example' # str | FHIR measure resource id for the measure you would like the parameter information for using the Measure.id field.
fhir_server_tenant_id_header = 'X-FHIR-TENANT-ID' # str | IBM FHIR Server uses HTTP headers to control which underlying tenant contains the data being retrieved. The default header name used to identify the tenant can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-TENANT-ID) is used. (optional) (default to X-FHIR-TENANT-ID)
fhir_data_source_id_header = 'X-FHIR-DSID' # str | IBM FHIR Server uses HTTP headers to control which underlying datasource contains the data being retrieved. The default header can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-DSID) is used. (optional) (default to X-FHIR-DSID)
fhir_data_source_id = 'fhir_data_source_id_example' # str | The id of the underlying datasource used by the FHIR server to contain the data. (optional)

try:
    # Get measure parameters by id
    api_response = api_instance.get_measure_parameters_by_id(version, fhir_server_rest_endpoint, fhir_server_tenant_id, measure_id, fhir_server_tenant_id_header=fhir_server_tenant_id_header, fhir_data_source_id_header=fhir_data_source_id_header, fhir_data_source_id=fhir_data_source_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling FHIRMeasuresApi->get_measure_parameters_by_id: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]
 **fhir_server_rest_endpoint** | **str**| The REST endpoint of the FHIR server the measure/libraries are stored in. For example: https://localhost:9443/fhir-server/api/v4 | [default to https://fhir-internal.dev:9443/fhir-server/api/v4]
 **fhir_server_tenant_id** | **str**| The id of the tenant used to store the measure/library information in the FHIR server.  | [default to default]
 **measure_id** | **str**| FHIR measure resource id for the measure you would like the parameter information for using the Measure.id field. | 
 **fhir_server_tenant_id_header** | **str**| IBM FHIR Server uses HTTP headers to control which underlying tenant contains the data being retrieved. The default header name used to identify the tenant can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-TENANT-ID) is used. | [optional] [default to X-FHIR-TENANT-ID]
 **fhir_data_source_id_header** | **str**| IBM FHIR Server uses HTTP headers to control which underlying datasource contains the data being retrieved. The default header can be changed by the user as needed for their execution environment. If no value is provided, the value in the base configuration files (X-FHIR-DSID) is used. | [optional] [default to X-FHIR-DSID]
 **fhir_data_source_id** | **str**| The id of the underlying datasource used by the FHIR server to contain the data. | [optional] 

### Return type

[**list[MeasureParameterInfoList]**](MeasureParameterInfoList.md)

### Authorization

[BasicAuth](../README.md#BasicAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

