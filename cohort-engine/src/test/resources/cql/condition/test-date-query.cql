library "Test" version '1.0.0'

using FHIR version '4.0.0'

include "FHIRHelpers" version '4.0.0' called FHIRHelpers  

context Patient

parameter MeasurementPeriod Interval<Date>

define ConditionInInterval:
	[Condition] c where c.recordedDate in "MeasurementPeriod"
	
	