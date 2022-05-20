library "Parent" version '1.0.0'
using "FHIR" version '4.0.0'

include "Child" version '1.0.0'

parameter MeasurementPeriod Interval<DateTime>
parameter InInitialPopulation Boolean default true
parameter InDenominator Boolean default true
parameter InDenominatorException Boolean default false
parameter InDenominatorExclusion Boolean default false
parameter InNumerator Boolean default false
parameter InNumeratorExclusion Boolean default false
parameter InCareGap1 Boolean default false
parameter InCareGap2 Boolean default true

context Patient

define "Initial Population":
	"InInitialPopulation"
	
define "Denominator":
	"InDenominator"
	
define "Numerator":
	Child."InNumerator"
	
define "Numerator Exclusion":
	"InNumeratorExclusion"
	
define "Denominator Exception":
	"InDenominatorException"
	
define "Denominator Exclusion":
	"InDenominatorExclusion"
	
define "CareGap1":
	"InCareGap1"
	
define "CareGap2":
	"InCareGap2"