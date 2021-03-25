# swagger_client.MeasuresApi

All URIs are relative to */services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**delete_evaluation**](MeasuresApi.md#delete_evaluation) | **DELETE** /v1/evaluation/{jobId} | Deletes a measure evaluation job
[**evaluate_measures**](MeasuresApi.md#evaluate_measures) | **POST** /v1/evaluation | Initiates evaluation of measures for given patients
[**get_evaluate_measures_results**](MeasuresApi.md#get_evaluate_measures_results) | **GET** /v1/evaluation/status/{jobId}/results | Measure evaluation job results
[**get_evaluate_measures_status**](MeasuresApi.md#get_evaluate_measures_status) | **GET** /v1/evaluation/status/{jobId} | Measure evaluation job status

# **delete_evaluation**
> delete_evaluation(version, job_id)

Deletes a measure evaluation job

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasuresApi()
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)
job_id = 'job_id_example' # str | Job identifier for measure evaluation request

try:
    # Deletes a measure evaluation job
    api_instance.delete_evaluation(version, job_id)
except ApiException as e:
    print("Exception when calling MeasuresApi->delete_evaluation: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]
 **job_id** | **str**| Job identifier for measure evaluation request | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **evaluate_measures**
> str evaluate_measures(body, version)

Initiates evaluation of measures for given patients

Asynchronous evaluation of measures for patients

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasuresApi()
body = swagger_client.MeasuresEvaluation() # MeasuresEvaluation | patients and the measures to run
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)

try:
    # Initiates evaluation of measures for given patients
    api_response = api_instance.evaluate_measures(body, version)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling MeasuresApi->evaluate_measures: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**MeasuresEvaluation**](MeasuresEvaluation.md)| patients and the measures to run | 
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]

### Return type

**str**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: */*
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_evaluate_measures_results**
> list[EvaluateMeasureResults] get_evaluate_measures_results(version, job_id)

Measure evaluation job results

Retrieves the results of the asynchronous measure evaluation job

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasuresApi()
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)
job_id = 'job_id_example' # str | Job identifier for measure evaluation request

try:
    # Measure evaluation job results
    api_response = api_instance.get_evaluate_measures_results(version, job_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling MeasuresApi->get_evaluate_measures_results: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]
 **job_id** | **str**| Job identifier for measure evaluation request | 

### Return type

[**list[EvaluateMeasureResults]**](EvaluateMeasureResults.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_evaluate_measures_status**
> list[EvaluateMeasuresStatus] get_evaluate_measures_status(version, job_id)

Measure evaluation job status

Retrieves the status of the asynchronous measure evaluation job

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasuresApi()
version = '2021-03-12' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2021-03-12)
job_id = 'job_id_example' # str | Job identifier for measure evaluation request

try:
    # Measure evaluation job status
    api_response = api_instance.get_evaluate_measures_status(version, job_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling MeasuresApi->get_evaluate_measures_status: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2021-03-12]
 **job_id** | **str**| Job identifier for measure evaluation request | 

### Return type

[**list[EvaluateMeasuresStatus]**](EvaluateMeasuresStatus.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

