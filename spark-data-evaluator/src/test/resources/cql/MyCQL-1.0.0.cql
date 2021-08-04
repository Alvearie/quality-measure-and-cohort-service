library MyCQL version '1.0.0'

using FHIR version '4.0.1'
include FHIRHelpers version '4.0.1'

parameter MyParameter Interval<DateTime>

context Patient

define MyDefine:
	[Patient] p where p.deceased during MyParameter
	
