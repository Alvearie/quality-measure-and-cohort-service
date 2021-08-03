library "exception" version '1.0.0'
using "FHIR" version '4.0.0'

// Intentionally omitting FHIRHelpers to force an exception
//include FHIRHelpers version '4.0.0'

valueset "vs" : 'http://some.io/condition'

context Patient
define "cohort":
	["Condition":"vs"]