library "FhirTest" version '1.0.0'

using "FHIR" version '4.0.0'
include "FHIRHelpers" version '4.0.0'

valueset "MyValueSet": 'uri:1:2:3'

context Patient

define MyBirthDate:
	Patient.birthDate

define MyCondition :
  First([Condition: MyValueSet])

define MyObservation:
  First([Observation] o)

define Aliased:
  [Condition] c
    let rd: c.recordedDate
    return
      { "date": rd }

define Joined:
  MyCondition c with [Encounter] e such that c.recordedDate after e.period.end
