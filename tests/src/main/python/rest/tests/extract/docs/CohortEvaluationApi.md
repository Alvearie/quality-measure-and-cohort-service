# swagger_client.CohortEvaluationApi

All URIs are relative to *https://localhost/services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**evaluate_cohort**](CohortEvaluationApi.md#evaluate_cohort) | **POST** /v1/cohort-evaluation | Evaluates a specific define within a CQL for a set of patients


# **evaluate_cohort**
> CohortResult evaluate_cohort(version, request_data, cql_definition)

Evaluates a specific define within a CQL for a set of patients

The body of the request is a multipart/form-data request with  an application/zip attachment named 'cql_definition' that contains the cohort cql definition to be evaluated.

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.CohortEvaluationApi()
version = '2022-02-18' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2022-02-18)
request_data = '/path/to/file.txt' # file | <p>A configuration file containing the information needed to process a cohort evaluation request. Two possible FHIR server endoints can be configured <code>dataServerConfig</code> and <code>terminologyServerConfig</code>. Only the <code>dataServerConfig</code> is required. If <code>terminologyServerConfig</code> is not provided, the connection details are assumed to be the same as the <code>dataServerConfig</code> connection.</p><p>The <code>defineToRun</code> will be the specific define of the given CQL to analyze the patients against</code></p><p>The <code>entrypoint</code> will be the cql file containing the define intended to run, as defined by the <code>defineToRun</code></p><p>The <code>patientIds</code> is a comma separated list of patients to run. Supplying a single patient does not need any trailing commas.</p><p>The parameter types and formats are described in detail in the <a href=\"http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id=parameter-formats\">user guide</a>.</p><p>The <code>loggingLevel</code> will determine how much and what type of logging to provide. The options are NA, COVERAGE, and TRACE.</p><p>Example Contents:   <pre>{     \"dataServerConfig\": {         \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",         \"endpoint\": \"ENDPOINT\",         \"user\": \"USER\",         \"password\": \"PASSWORD\",         \"logInfo\": [             \"REQUEST_SUMMARY\",             \"RESPONSE_SUMMARY\"         ],         \"tenantId\": \"default\"     },     \"patientIds\": \"PATIENTIDS\",      \"parameters\": {             \"Measurement Period\": {                 \"type\": \"interval\",                 \"start\": {                     \"type\": \"date\",                     \"value\": \"2019-07-04\"                 },                 \"startInclusive\": true,                 \"end\": {                     \"type\": \"date\",                     \"value\": \"2020-07-04\"                 },                 \"endInclusive\": true     },     \"entrypoint\": Test-1.0.0.cql     \"defineToRun\": InitialPopulation     \"loggingLevel\": NA }</pre></p>
cql_definition = '/path/to/file.txt' # file | A zip file containing the cohort definition to run. NOTE: The name of the file must follow the convention [NAME]-[VERSION].zip

try:
    # Evaluates a specific define within a CQL for a set of patients
    api_response = api_instance.evaluate_cohort(version, request_data, cql_definition)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling CohortEvaluationApi->evaluate_cohort: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2022-02-18]
 **request_data** | **file**| &lt;p&gt;A configuration file containing the information needed to process a cohort evaluation request. Two possible FHIR server endoints can be configured &lt;code&gt;dataServerConfig&lt;/code&gt; and &lt;code&gt;terminologyServerConfig&lt;/code&gt;. Only the &lt;code&gt;dataServerConfig&lt;/code&gt; is required. If &lt;code&gt;terminologyServerConfig&lt;/code&gt; is not provided, the connection details are assumed to be the same as the &lt;code&gt;dataServerConfig&lt;/code&gt; connection.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;defineToRun&lt;/code&gt; will be the specific define of the given CQL to analyze the patients against&lt;/code&gt;&lt;/p&gt;&lt;p&gt;The &lt;code&gt;entrypoint&lt;/code&gt; will be the cql file containing the define intended to run, as defined by the &lt;code&gt;defineToRun&lt;/code&gt;&lt;/p&gt;&lt;p&gt;The &lt;code&gt;patientIds&lt;/code&gt; is a comma separated list of patients to run. Supplying a single patient does not need any trailing commas.&lt;/p&gt;&lt;p&gt;The parameter types and formats are described in detail in the &lt;a href&#x3D;\&quot;http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id&#x3D;parameter-formats\&quot;&gt;user guide&lt;/a&gt;.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;loggingLevel&lt;/code&gt; will determine how much and what type of logging to provide. The options are NA, COVERAGE, and TRACE.&lt;/p&gt;&lt;p&gt;Example Contents:   &lt;pre&gt;{     \&quot;dataServerConfig\&quot;: {         \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,         \&quot;endpoint\&quot;: \&quot;ENDPOINT\&quot;,         \&quot;user\&quot;: \&quot;USER\&quot;,         \&quot;password\&quot;: \&quot;PASSWORD\&quot;,         \&quot;logInfo\&quot;: [             \&quot;REQUEST_SUMMARY\&quot;,             \&quot;RESPONSE_SUMMARY\&quot;         ],         \&quot;tenantId\&quot;: \&quot;default\&quot;     },     \&quot;patientIds\&quot;: \&quot;PATIENTIDS\&quot;,      \&quot;parameters\&quot;: {             \&quot;Measurement Period\&quot;: {                 \&quot;type\&quot;: \&quot;interval\&quot;,                 \&quot;start\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2019-07-04\&quot;                 },                 \&quot;startInclusive\&quot;: true,                 \&quot;end\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2020-07-04\&quot;                 },                 \&quot;endInclusive\&quot;: true     },     \&quot;entrypoint\&quot;: Test-1.0.0.cql     \&quot;defineToRun\&quot;: InitialPopulation     \&quot;loggingLevel\&quot;: NA }&lt;/pre&gt;&lt;/p&gt; | 
 **cql_definition** | **file**| A zip file containing the cohort definition to run. NOTE: The name of the file must follow the convention [NAME]-[VERSION].zip | 

### Return type

[**CohortResult**](CohortResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

