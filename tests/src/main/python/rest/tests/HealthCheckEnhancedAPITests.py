from __future__ import absolute_import
import sys
import os
import re
from datetime import date
from utilities.TestClass import TestClass
import swagger_client
from swagger_client.api.status_api import StatusApi
from swagger_client.rest import ApiException

# Set global variables in the file that can be accessed by all tests within the class
fhir_svr_conn_cfg_path = "/bzt-configs/tests/src/main/resources/config/"

class HealthCheckEnhancedAPITests(TestClass):

	def test_withValidFhirDataAndTerminologyServerConnectionConfigs(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "health-check-enhanced-api-fhir-server-conn-config-valid-data-and-terminology.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code and successful connection status to both the data server and the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		assert expectedResp in response, 'Response should contain ' + expectedResp
		
	def test_withOnlyFhirDataServerConnectionConfigProvided(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "health-check-enhanced-api-fhir-server-conn-config-no-terminology-config.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with successful connection to the data server and notAttempted connection status to the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"notAttempted","serverConfigType":"terminologyServerConfig"}}'
		assert expectedResp in response, 'Response should contain ' + expectedResp
		
	def test_withOnlyTerminologyServerConnectionConfigProvided(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		result = None
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "health-check-enhanced-api-fhir-server-conn-config-no-data-config.json"
		try:
			resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
			result = resp.read()
		except ApiException as e: 
			print("Exception when calling HealthCheckEnhancedApi->health_check_enhanced: %s\n" % e)
			result = str(e)
			print("Exception in test_withOnlyTerminologyServerConnectionConfigProvided is : " + result)
		assert '400' in result, 'Should contain a 400 error from COHORT_SERVICE with the error message stating: HV000116: The object to be validated must not be null.'
		
	def test_invalidHostnameInEndPointURLForDataServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_endpoint_host-dataserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: failure and terminology server connectionResults: success and at least one error code 404 from the COHORT_SERVICE in the errors list for the data server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"failure","serverConfigType":"dataServerConfig","serviceErrorList":{"errorSource":"COHORT_SERVICE","errors":[{"code":500,"correlationId":"","description":"Reason: FhirClientConnectionException","level":"ERROR","message":"Failed to retrieve the server metadata statement during client initialization. URL used was https://fhir-internal.dev.sv:9443/fhir-server/api/v4/metadata"},{"code":500,"correlationId":"","level":"ERROR","message":"Failed to parse response from server when performing GET to URL https://fhir-internal.dev.sv:9443/fhir-server/api/v4/metadata?_format=json - java.net.UnknownHostException: fhir-internal.dev.sv: Name or service not known"},{"code":404,"correlationId":"","level":"ERROR","message":"fhir-internal.dev.sv: Name or service not known"}],"statusCode":500,"trace":""}},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidHostnameInEndPointURLForTerminologyServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_endpoint_host-terminologyserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: success and terminology server connectionResults: failure and at least one error code 404 from the COHORT_SERVICE in the errors list for the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"failure","serverConfigType":"terminologyServerConfig","serviceErrorList":{"errorSource":"COHORT_SERVICE","errors":[{"code":500,"correlationId":"","description":"Reason: FhirClientConnectionException","level":"ERROR","message":"Failed to retrieve the server metadata statement during client initialization. URL used was https://fhir-internal.dev.sv:9443/fhir-server/api/v4/metadata"},{"code":500,"correlationId":"","level":"ERROR","message":"Failed to parse response from server when performing GET to URL https://fhir-internal.dev.sv:9443/fhir-server/api/v4/metadata?_format=json - java.net.UnknownHostException: fhir-internal.dev.sv"},{"code":404,"correlationId":"","level":"ERROR","message":"fhir-internal.dev.sv"}],"statusCode":500,"trace":""}}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidPortInEndPointURLForDataServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_endpoint_port-dataserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: failure and terminology server connectionResults: success and at least one error code 504 from COHORT_SERVICE in the errors list for the data server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"failure","serverConfigType":"dataServerConfig","serviceErrorList":{"errorSource":"COHORT_SERVICE","errors":[{"code":500,"correlationId":"","description":"Reason: FhirClientConnectionException","level":"ERROR","message":"Failed to retrieve the server metadata statement during client initialization. URL used was https://fhir-internal.dev.svc:9444/fhir-server/api/v4/metadata"},{"code":500,"correlationId":"","level":"ERROR","message":"Failed to parse response from server when performing GET to URL https://fhir-internal.dev.svc:9444/fhir-server/api/v4/metadata?_format=json - org.apache.http.conn.ConnectTimeoutException: Connect to fhir-internal.dev.svc:9444 [fhir-internal.dev.svc/172.21.235.190] failed: connect timed out"},{"code":504,"correlationId":"","level":"ERROR","message":"Connect to fhir-internal.dev.svc:9444 [fhir-internal.dev.svc/172.21.235.190] failed: connect timed out"},{"code":504,"correlationId":"","level":"ERROR","message":"connect timed out"}],"statusCode":500,"trace":""}},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidPortInEndPointURLForTerminologyServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_endpoint_port-terminologyserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: success and terminology server connectionResults: failure and at least one error code 504 from COHORT_SERVICE in the errors list for the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"failure","serverConfigType":"terminologyServerConfig","serviceErrorList":{"errorSource":"COHORT_SERVICE","errors":[{"code":500,"correlationId":"","description":"Reason: FhirClientConnectionException","level":"ERROR","message":"Failed to retrieve the server metadata statement during client initialization. URL used was https://fhir-internal.dev.svc:9444/fhir-server/api/v4/metadata"},{"code":500,"correlationId":"","level":"ERROR","message":"Failed to parse response from server when performing GET to URL https://fhir-internal.dev.svc:9444/fhir-server/api/v4/metadata?_format=json - org.apache.http.conn.ConnectTimeoutException: Connect to fhir-internal.dev.svc:9444 [fhir-internal.dev.svc/172.21.235.190] failed: connect timed out"},{"code":504,"correlationId":"","level":"ERROR","message":"Connect to fhir-internal.dev.svc:9444 [fhir-internal.dev.svc/172.21.235.190] failed: connect timed out"},{"code":504,"correlationId":"","level":"ERROR","message":"connect timed out"}],"statusCode":500,"trace":""}}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidFHIRUserSpecifiedInDataServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_fhir_user-dataserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: failure and terminology server connectionResults: success and error code 401 returned from FHIR_SERVER in the errors list for the data server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"failure","serverConfigType":"dataServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":401,"correlationId":"","description":"Could not authenticate with FHIR server.","level":"ERROR","message":"HTTP 401 Unauthorized"}],"statusCode":400,"trace":""}},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidFHIRUserSpecifiedInTerminologyServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_fhir_user-terminologyserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: success and terminology server connectionResults: failure and error code 401 returned from FHIR_SERVER in the errors list for the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"failure","serverConfigType":"terminologyServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":401,"correlationId":"","description":"Could not authenticate with FHIR server.","level":"ERROR","message":"HTTP 401 Unauthorized"}],"statusCode":400,"trace":""}}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidFHIRUserPasswordSpecifiedInDataServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_fhir_user_password-dataserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: failure and terminology server connectionResults: success and error code 401 returned from FHIR_SERVER in the errors list for the data server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"failure","serverConfigType":"dataServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":401,"correlationId":"","description":"Could not authenticate with FHIR server.","level":"ERROR","message":"HTTP 401 Unauthorized"}],"statusCode":400,"trace":""}},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidFHIRUserPasswordSpecifiedInTerminologyServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_fhir_user_password-terminologyserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: success and terminology server connectionResults: failure and error code 401 returned from FHIR_SERVER in the errors list for the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"failure","serverConfigType":"terminologyServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":401,"correlationId":"","description":"Could not authenticate with FHIR server.","level":"ERROR","message":"HTTP 401 Unauthorized"}],"statusCode":400,"trace":""}}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidTenantIdSpecifiedInDataServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_tenantid-dataserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: failure and terminology server connectionResults: success and error code 400 returned from FHIR_SERVER in the errors list for the data server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"failure","serverConfigType":"dataServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":400,"correlationId":"","description":"{\\\"resourceType\\\":\\\"OperationOutcome\\\",\\\"issue\\\":[{\\\"severity\\\":\\\"fatal\\\",\\\"code\\\":\\\"invalid\\\",\\\"details\\\":{\\\"text\\\":\\\"FHIRException: Tenant configuration does not exist: test-fvtt\\\"}}]}","level":"ERROR","message":"Exception while communicating with FHIR"}],"statusCode":400,"trace":""}},"terminologyServerConnectionResults":{"connectionResults":"success","serverConfigType":"terminologyServerConfig"}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_invalidTenantIdSpecifiedInTerminologyServerConfig(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		version = date.today()
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "fhir-server-conn-config-invalid_tenantid-terminologyserverconfig.json"
		resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
		response = resp.read()
		response = response.decode('utf-8')
		print("response is : " + str(response))
		status = resp.status
		print("status from response is : " + str(status))
		assert '200' in str(status), 'Should contain a 200 server response code with data server connectionResults: success and terminology server connectionResults: failure and error code 400 returned from FHIR_SERVER in the errors list for the terminology server.'
		expectedResp = '{"dataServerConnectionResults":{"connectionResults":"success","serverConfigType":"dataServerConfig"},"terminologyServerConnectionResults":{"connectionResults":"failure","serverConfigType":"terminologyServerConfig","serviceErrorList":{"errorSource":"FHIR_SERVER","errors":[{"code":400,"correlationId":"","description":"{\\\"resourceType\\\":\\\"OperationOutcome\\\",\\\"issue\\\":[{\\\"severity\\\":\\\"fatal\\\",\\\"code\\\":\\\"invalid\\\",\\\"details\\\":{\\\"text\\\":\\\"FHIRException: Tenant configuration does not exist: knowledge1\\\"}}]}","level":"ERROR","message":"Exception while communicating with FHIR"}],"statusCode":400,"trace":""}}}'
		actualResp = re.sub('\"correlationId\":\"[a-z\-0-9]+\"','\"correlationId\":\"\"',response)
		actualResp = re.sub('\"trace\":\"[a-z\-0-9]+\"','\"trace\":\"\"',actualResp)
		assert expectedResp in actualResp, 'The response from API should contain ' + expectedResp
		
	def test_healthCheckEnhancedAPIWithInvalidVersion(self):
		healthCheckEnhancedApi = StatusApi(swagger_client.ApiClient(self.configuration))
		result = None
		version = '02-23-2022'
		fhir_svr_conn_cfg = fhir_svr_conn_cfg_path + "health-check-enhanced-api-fhir-server-conn-config-valid-data-and-terminology.json"
		try:
			resp = healthCheckEnhancedApi.health_check_enhanced(version, fhir_svr_conn_cfg, _preload_content=False)
			result = resp.read()
		except ApiException as e: 
			print("Exception when calling HealthCheckEnhancedApi->health_check_enhanced: %s\n" % e)
			result = str(e)
			print("Exception in test_healthCheckEnhancedAPIWithInvalidVersion is : " + result)
		assert '400' in result, 'Should contain a 400 error with an error message stating: Bad Request and message description stating: Invalid version parameter value.'