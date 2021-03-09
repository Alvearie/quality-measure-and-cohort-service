library "TestAdultMales" version '1.0.0'
using "FHIR" version '4.0.0'

parameter MeasurementPeriod Interval<DateTime>
parameter SomeAge Integer

context Patient

define "Over 18":
	AgeInYears() >= SomeAge + 1

define "Initial Population":
	"Over 18"

define "Denominator":
	"Initial Population"

define "Numerator":
	"Male"

define "Male":
	Patient.gender.value = 'male'
