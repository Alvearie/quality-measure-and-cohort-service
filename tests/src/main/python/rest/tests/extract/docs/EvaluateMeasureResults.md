# EvaluateMeasureResults

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**tenant** | **str** | Tenant the measure was evaluated for | [optional] 
**identifier** | **str** | Additional identifier for the MeasureReport | [optional] 
**status** | **str** | complete | pending | error | [optional] 
**type** | **str** | individual | subject-list | summary | data-collection | [optional] 
**measure** | **str** | What measure was calculated (measureId) | [optional] 
**subject** | **list[str]** | What individual(s) the report is for (subjectId) (Patient | Practitioner | PractitionerRole | Location | Device | RelatedPerson | Group | [optional] 
**_date** | **datetime** | When the report was generated | [optional] 
**reporter** | **str** | Who is reporting the data | [optional] 
**period** | [**PatientMeasureEvaluationPeriod**](PatientMeasureEvaluationPeriod.md) |  | [optional] 
**improvement_notation** | **str** | increase | decrease | [optional] 
**group** | [**list[PatientMeasureEvaluationGroup]**](PatientMeasureEvaluationGroup.md) |  | [optional] 
**evaluated_resource** | **list[str]** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

