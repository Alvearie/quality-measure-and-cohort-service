library "TestDummyPopulations" version '1.0.0'
using "FHIR" version '4.0.0'

parameter MeasurementPeriod Interval<DateTime>

context Patient

define "InitialPopulation":
	true
define "Denominator":
	true
define "Numerator":
	false
define "DenominatorExclusions":
	false