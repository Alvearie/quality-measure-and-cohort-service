# swagger_client.MeasureEvaluationApi

All URIs are relative to *https://localhost/services/cohort/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**evaluate_measure**](MeasureEvaluationApi.md#evaluate_measure) | **POST** /v1/evaluation | Evaluates a measure bundle for a single patient
[**evaluate_patient_list_measure**](MeasureEvaluationApi.md#evaluate_patient_list_measure) | **POST** /v1/evaluation-patient-list | Evaluates a measure bundle for a list of patients


# **evaluate_measure**
> evaluate_measure(version, request_data, measure)

Evaluates a measure bundle for a single patient

The body of the request is a multipart/form-data request with an application/json attachment named 'request_data' that describes the measure evaluation that will be performed and an application/zip attachment named 'measure' that contains the measure and library artifacts to be evaluated. Valueset resources required for Measure evaluation must be loaded to the FHIR server in advance of an evaluation request. Examples of the response measure reports (individual and patient list) can be found as part the FHIR IG: https://www.hl7.org/fhir/measurereport-examples.html.

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasureEvaluationApi()
version = '2022-02-18' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2022-02-18)
request_data = '/path/to/file.txt' # file | <p>A configuration file containing the information needed to process a measure evaluation request. Two possible FHIR server endoints can be configured <code>dataServerConfig</code> and <code>terminologyServerConfig</code>. Only the <code>dataServerConfig</code> is required. If <code>terminologyServerConfig</code> is not provided, the connection details are assumed to be the same as the <code>dataServerConfig</code> connection.</p><p>The <code>measureContext.measureId</code> field can be a FHIR resource ID or canonical URL. Alternatively, <code>measureContext.identifier</code> and <code>measureContext.version</code> can be used to lookup the measure based on a business identifier found in the resource definition. Only one of measureId or identifier + version should be specified. Canonical URL is the recommended lookup mechanism.</p><p>The parameter types and formats are described in detail in the <a href=\"http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id=parameter-formats\">user guide</a>.</p><p>The <code>evidenceOptions</code> controls the granularity of evidence data to be written to the FHIR MeasureReport. The <code>expandValueSets</code> flag is used to control whether or not the terminology provider is used to expand ValueSet references or if the FHIR :in modifier is used during search requests. The FHIR :in modifier is supported in IBM FHIR 4.7.0 and above. The default behavior is to expand value sets using the terminology provider in order to cover the widest range of FHIR server functionality. A value of false can be used to improve search performance if terminology resources are available on the data server and it supports the :in modifier. The <code>searchPageSize</code> controls how many data records are retrieved per request during FHIR search API execution. The default value for this setting is small in most servers and performance can be boosted by larger values. The default is 1000 which is the maximum allowed page size in IBM FHIR.</p><p>For more detailed information about the evidence options, check out measure evaluation <a href=\"https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/getting-started?id=evidence-options\">documentation</a> here.</p><p>Example Contents:   <pre>{     \"dataServerConfig\": {         \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",         \"endpoint\": \"ENDPOINT\",         \"user\": \"USER\",         \"password\": \"PASSWORD\",         \"logInfo\": [             \"REQUEST_SUMMARY\",             \"RESPONSE_SUMMARY\"         ],         \"tenantId\": \"default\"     },     \"patientId\": \"PATIENTID\",     \"measureContext\": {         \"measureId\": \"MEASUREID\",         \"identifier\": null,         \"version\": null,         \"parameters\": {             \"Measurement Period\": {                 \"type\": \"interval\",                 \"start\": {                     \"type\": \"date\",                     \"value\": \"2019-07-04\"                 },                 \"startInclusive\": true,                 \"end\": {                     \"type\": \"date\",                     \"value\": \"2020-07-04\"                 },                 \"endInclusive\": true             }         }     },     \"evidenceOptions\": {         \"includeEvaluatedResources\": false,         \"defineReturnOption\": \"ALL\"     },     \"expandValueSets\": true     \"searchPageSize\": 1000 }</pre></p>
measure = '/path/to/file.txt' # file | A file in ZIP format that contains the FHIR resources to use in the evaluation. This should contain all the FHIR Measure and Library resources needed in a particular directory structure as follows:<pre>fhirResources/MeasureName-MeasureVersion.json fhirResources/libraries/LibraryName1-LibraryVersion.json fhirResources/libraries/LibraryName2-LibraryVersion.json etc. </pre>

try:
    # Evaluates a measure bundle for a single patient
    api_instance.evaluate_measure(version, request_data, measure)
except ApiException as e:
    print("Exception when calling MeasureEvaluationApi->evaluate_measure: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2022-02-18]
 **request_data** | **file**| &lt;p&gt;A configuration file containing the information needed to process a measure evaluation request. Two possible FHIR server endoints can be configured &lt;code&gt;dataServerConfig&lt;/code&gt; and &lt;code&gt;terminologyServerConfig&lt;/code&gt;. Only the &lt;code&gt;dataServerConfig&lt;/code&gt; is required. If &lt;code&gt;terminologyServerConfig&lt;/code&gt; is not provided, the connection details are assumed to be the same as the &lt;code&gt;dataServerConfig&lt;/code&gt; connection.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;measureContext.measureId&lt;/code&gt; field can be a FHIR resource ID or canonical URL. Alternatively, &lt;code&gt;measureContext.identifier&lt;/code&gt; and &lt;code&gt;measureContext.version&lt;/code&gt; can be used to lookup the measure based on a business identifier found in the resource definition. Only one of measureId or identifier + version should be specified. Canonical URL is the recommended lookup mechanism.&lt;/p&gt;&lt;p&gt;The parameter types and formats are described in detail in the &lt;a href&#x3D;\&quot;http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id&#x3D;parameter-formats\&quot;&gt;user guide&lt;/a&gt;.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;evidenceOptions&lt;/code&gt; controls the granularity of evidence data to be written to the FHIR MeasureReport. The &lt;code&gt;expandValueSets&lt;/code&gt; flag is used to control whether or not the terminology provider is used to expand ValueSet references or if the FHIR :in modifier is used during search requests. The FHIR :in modifier is supported in IBM FHIR 4.7.0 and above. The default behavior is to expand value sets using the terminology provider in order to cover the widest range of FHIR server functionality. A value of false can be used to improve search performance if terminology resources are available on the data server and it supports the :in modifier. The &lt;code&gt;searchPageSize&lt;/code&gt; controls how many data records are retrieved per request during FHIR search API execution. The default value for this setting is small in most servers and performance can be boosted by larger values. The default is 1000 which is the maximum allowed page size in IBM FHIR.&lt;/p&gt;&lt;p&gt;For more detailed information about the evidence options, check out measure evaluation &lt;a href&#x3D;\&quot;https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/getting-started?id&#x3D;evidence-options\&quot;&gt;documentation&lt;/a&gt; here.&lt;/p&gt;&lt;p&gt;Example Contents:   &lt;pre&gt;{     \&quot;dataServerConfig\&quot;: {         \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,         \&quot;endpoint\&quot;: \&quot;ENDPOINT\&quot;,         \&quot;user\&quot;: \&quot;USER\&quot;,         \&quot;password\&quot;: \&quot;PASSWORD\&quot;,         \&quot;logInfo\&quot;: [             \&quot;REQUEST_SUMMARY\&quot;,             \&quot;RESPONSE_SUMMARY\&quot;         ],         \&quot;tenantId\&quot;: \&quot;default\&quot;     },     \&quot;patientId\&quot;: \&quot;PATIENTID\&quot;,     \&quot;measureContext\&quot;: {         \&quot;measureId\&quot;: \&quot;MEASUREID\&quot;,         \&quot;identifier\&quot;: null,         \&quot;version\&quot;: null,         \&quot;parameters\&quot;: {             \&quot;Measurement Period\&quot;: {                 \&quot;type\&quot;: \&quot;interval\&quot;,                 \&quot;start\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2019-07-04\&quot;                 },                 \&quot;startInclusive\&quot;: true,                 \&quot;end\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2020-07-04\&quot;                 },                 \&quot;endInclusive\&quot;: true             }         }     },     \&quot;evidenceOptions\&quot;: {         \&quot;includeEvaluatedResources\&quot;: false,         \&quot;defineReturnOption\&quot;: \&quot;ALL\&quot;     },     \&quot;expandValueSets\&quot;: true     \&quot;searchPageSize\&quot;: 1000 }&lt;/pre&gt;&lt;/p&gt; | 
 **measure** | **file**| A file in ZIP format that contains the FHIR resources to use in the evaluation. This should contain all the FHIR Measure and Library resources needed in a particular directory structure as follows:&lt;pre&gt;fhirResources/MeasureName-MeasureVersion.json fhirResources/libraries/LibraryName1-LibraryVersion.json fhirResources/libraries/LibraryName2-LibraryVersion.json etc. &lt;/pre&gt; | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **evaluate_patient_list_measure**
> evaluate_patient_list_measure(version, request_data, measure)

Evaluates a measure bundle for a list of patients

The body of the request is a multipart/form-data request with an application/json attachment named 'request_data' that describes the measure evaluation that will be performed and an application/zip attachment named 'measure' that contains the measure and library artifacts to be evaluated. Valueset resources required for Measure evaluation must be loaded to the FHIR server in advance of an evaluation request. Examples of the response measure reports (individual and patient list) can be found as part the FHIR IG: https://www.hl7.org/fhir/measurereport-examples.html.

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.MeasureEvaluationApi()
version = '2022-02-18' # str | The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (default to 2022-02-18)
request_data = '/path/to/file.txt' # file | <p>A configuration file containing the information needed to process a measure evaluation request. Two possible FHIR server endoints can be configured <code>dataServerConfig</code> and <code>terminologyServerConfig</code>. Only the <code>dataServerConfig</code> is required. If <code>terminologyServerConfig</code> is not provided, the connection details are assumed to be the same as the <code>dataServerConfig</code> connection.</p><p>The <code>measureContext.measureId</code> field can be a FHIR resource ID or canonical URL. Alternatively, <code>measureContext.identifier</code> and <code>measureContext.version</code> can be used to lookup the measure based on a business identifier found in the resource definition. Only one of measureId or identifier + version should be specified. Canonical URL is the recommended lookup mechanism.</p><p>The parameter types and formats are described in detail in the <a href=\"http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id=parameter-formats\">user guide</a>.</p><p>The <code>evidenceOptions</code> controls the granularity of evidence data to be written to the FHIR MeasureReport. The <code>expandValueSets</code> flag is used to control whether or not the terminology provider is used to expand ValueSet references or if the FHIR :in modifier is used during search requests. The FHIR :in modifier is supported in IBM FHIR 4.7.0 and above. The default behavior is to expand value sets using the terminology provider in order to cover the widest range of FHIR server functionality. A value of false can be used to improve search performance if terminology resources are available on the data server and it supports the :in modifier. The <code>searchPageSize</code> controls how many data records are retrieved per request during FHIR search API execution. The default value for this setting is small in most servers and performance can be boosted by larger values. The default is 1000 which is the maximum allowed page size in IBM FHIR.</p><p>For more detailed information about the evidence options, check out measure evaluation <a href=\"https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/getting-started?id=evidence-options\">documentation</a> here.</p><p>Example Contents:   <pre>{     \"dataServerConfig\": {         \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",         \"endpoint\": \"ENDPOINT\",         \"user\": \"USER\",         \"password\": \"PASSWORD\",         \"logInfo\": [             \"REQUEST_SUMMARY\",             \"RESPONSE_SUMMARY\"         ],         \"tenantId\": \"default\"     },     \"patientIds\": [         \"PATIENT_ID_1\",         \"PATIENT_ID_2\"     ],     \"measureContext\": {         \"measureId\": \"MEASUREID\",         \"identifier\": null,         \"version\": null,         \"parameters\": {             \"Measurement Period\": {                 \"type\": \"interval\",                 \"start\": {                     \"type\": \"date\",                     \"value\": \"2019-07-04\"                 },                 \"startInclusive\": true,                 \"end\": {                     \"type\": \"date\",                     \"value\": \"2020-07-04\"                 },                 \"endInclusive\": true             }         }     },     \"evidenceOptions\": {         \"includeEvaluatedResources\": false,         \"defineReturnOption\": \"ALL\"     },     \"expandValueSets\": true     \"searchPageSize\": 1000 }</pre></p>
measure = '/path/to/file.txt' # file | A file in ZIP format that contains the FHIR resources to use in the evaluation. This should contain all the FHIR Measure and Library resources needed in a particular directory structure as follows:<pre>fhirResources/MeasureName-MeasureVersion.json fhirResources/libraries/LibraryName1-LibraryVersion.json fhirResources/libraries/LibraryName2-LibraryVersion.json etc. </pre>

try:
    # Evaluates a measure bundle for a list of patients
    api_instance.evaluate_patient_list_measure(version, request_data, measure)
except ApiException as e:
    print("Exception when calling MeasureEvaluationApi->evaluate_patient_list_measure: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **version** | **str**| The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. | [default to 2022-02-18]
 **request_data** | **file**| &lt;p&gt;A configuration file containing the information needed to process a measure evaluation request. Two possible FHIR server endoints can be configured &lt;code&gt;dataServerConfig&lt;/code&gt; and &lt;code&gt;terminologyServerConfig&lt;/code&gt;. Only the &lt;code&gt;dataServerConfig&lt;/code&gt; is required. If &lt;code&gt;terminologyServerConfig&lt;/code&gt; is not provided, the connection details are assumed to be the same as the &lt;code&gt;dataServerConfig&lt;/code&gt; connection.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;measureContext.measureId&lt;/code&gt; field can be a FHIR resource ID or canonical URL. Alternatively, &lt;code&gt;measureContext.identifier&lt;/code&gt; and &lt;code&gt;measureContext.version&lt;/code&gt; can be used to lookup the measure based on a business identifier found in the resource definition. Only one of measureId or identifier + version should be specified. Canonical URL is the recommended lookup mechanism.&lt;/p&gt;&lt;p&gt;The parameter types and formats are described in detail in the &lt;a href&#x3D;\&quot;http://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats?id&#x3D;parameter-formats\&quot;&gt;user guide&lt;/a&gt;.&lt;/p&gt;&lt;p&gt;The &lt;code&gt;evidenceOptions&lt;/code&gt; controls the granularity of evidence data to be written to the FHIR MeasureReport. The &lt;code&gt;expandValueSets&lt;/code&gt; flag is used to control whether or not the terminology provider is used to expand ValueSet references or if the FHIR :in modifier is used during search requests. The FHIR :in modifier is supported in IBM FHIR 4.7.0 and above. The default behavior is to expand value sets using the terminology provider in order to cover the widest range of FHIR server functionality. A value of false can be used to improve search performance if terminology resources are available on the data server and it supports the :in modifier. The &lt;code&gt;searchPageSize&lt;/code&gt; controls how many data records are retrieved per request during FHIR search API execution. The default value for this setting is small in most servers and performance can be boosted by larger values. The default is 1000 which is the maximum allowed page size in IBM FHIR.&lt;/p&gt;&lt;p&gt;For more detailed information about the evidence options, check out measure evaluation &lt;a href&#x3D;\&quot;https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/getting-started?id&#x3D;evidence-options\&quot;&gt;documentation&lt;/a&gt; here.&lt;/p&gt;&lt;p&gt;Example Contents:   &lt;pre&gt;{     \&quot;dataServerConfig\&quot;: {         \&quot;@class\&quot;: \&quot;com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\&quot;,         \&quot;endpoint\&quot;: \&quot;ENDPOINT\&quot;,         \&quot;user\&quot;: \&quot;USER\&quot;,         \&quot;password\&quot;: \&quot;PASSWORD\&quot;,         \&quot;logInfo\&quot;: [             \&quot;REQUEST_SUMMARY\&quot;,             \&quot;RESPONSE_SUMMARY\&quot;         ],         \&quot;tenantId\&quot;: \&quot;default\&quot;     },     \&quot;patientIds\&quot;: [         \&quot;PATIENT_ID_1\&quot;,         \&quot;PATIENT_ID_2\&quot;     ],     \&quot;measureContext\&quot;: {         \&quot;measureId\&quot;: \&quot;MEASUREID\&quot;,         \&quot;identifier\&quot;: null,         \&quot;version\&quot;: null,         \&quot;parameters\&quot;: {             \&quot;Measurement Period\&quot;: {                 \&quot;type\&quot;: \&quot;interval\&quot;,                 \&quot;start\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2019-07-04\&quot;                 },                 \&quot;startInclusive\&quot;: true,                 \&quot;end\&quot;: {                     \&quot;type\&quot;: \&quot;date\&quot;,                     \&quot;value\&quot;: \&quot;2020-07-04\&quot;                 },                 \&quot;endInclusive\&quot;: true             }         }     },     \&quot;evidenceOptions\&quot;: {         \&quot;includeEvaluatedResources\&quot;: false,         \&quot;defineReturnOption\&quot;: \&quot;ALL\&quot;     },     \&quot;expandValueSets\&quot;: true     \&quot;searchPageSize\&quot;: 1000 }&lt;/pre&gt;&lt;/p&gt; | 
 **measure** | **file**| A file in ZIP format that contains the FHIR resources to use in the evaluation. This should contain all the FHIR Measure and Library resources needed in a particular directory structure as follows:&lt;pre&gt;fhirResources/MeasureName-MeasureVersion.json fhirResources/libraries/LibraryName1-LibraryVersion.json fhirResources/libraries/LibraryName2-LibraryVersion.json etc. &lt;/pre&gt; | 

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

