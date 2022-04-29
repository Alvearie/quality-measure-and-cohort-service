library "Test Continuous Variable" version '5.0.0'

/*
This CQL is not currently used by any tests as the cqf-ruler support of contiuos-variable scoring was found to be incomplete.  Leaving this CQL for any potential future addition of said functionality.
*/

using "FHIR" version '4.0.0'

include FHIRHelpers version '1.0.0' called FHIRHelpers

parameter "Measurement Period" Interval<DateTime>

context Patient

define "Initial Population" :
  AgeInYears() >= 18

define "Measure Population" :
  "Initial Population"

define "Measure Population Exclusion" :
  Patient.gender.value = 'unknown'

define function "GetId"(uri String):
	Last(Split(uri, '/'))

define function "Measure Observation" (Encounter "Encounter" ) :
  duration in minutes of Encounter.period