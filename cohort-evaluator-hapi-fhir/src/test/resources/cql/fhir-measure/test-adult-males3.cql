library "TestAdultMales3" version '1.0.0'
using "FHIR" version '4.0.0'

parameter MeasurementPeriod Interval<DateTime>

context Patient

define "Over 18":
	AgeInYears() >= 18
	
define "Male":
	Patient.gender.value = 'male'