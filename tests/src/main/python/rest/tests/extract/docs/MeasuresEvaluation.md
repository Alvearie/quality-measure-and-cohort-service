# MeasuresEvaluation

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**patients_tenant_id** | **str** | Tenant identifier for the tenant these patients are associated with | [optional] 
**patients_server_url** | **str** | URL specifying the server used to store the patients | [optional] 
**patient_server_connection_properties** | **list[str]** | A list of connection property strings to be used for the measure server | [optional] 
**measure_tenant_id** | **str** | Tenant identifier for the tenant this measure is associated with | 
**measure_server_url** | **str** | URL specifying the server used to store the measure | 
**measure_server_connection_properties** | **list[str]** | A list of connection property strings to be used for the measure server | [optional] 
**results_valid_til** | **int** | Number of minutes the job results will be available after the job completes | [optional] 
**measure_evaluations** | [**list[PatientMeasureEvaluations]**](PatientMeasureEvaluations.md) |  | 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

