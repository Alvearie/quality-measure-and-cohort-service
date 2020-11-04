library "Test" version '1.0.0'

using FHIR version '4.0.0'

include "FHIRHelpers" version '4.0.0' called FHIRHelpers  

parameter "MeasurementPeriod" Interval<DateTime>

context Patient

define ConditionInInterval:
	[Condition] c where c.recordedDate in "MeasurementPeriod"