library "TestAdultMalesSimple" version '1.0.0'
using "FHIR" version '4.0.0'
include "FHIRHelpers" version '1.0.0'

parameter MeasurementPeriod Interval<DateTime>
valueset "Married": 'urn:oid:MarriedStatus'
valueset "Type2Diabetes": 'urn:oid:Type2Diabetes'

context Patient

define "Over 18":
	AgeInYears() >= 18

define "Initial Population":
	"Over 18"

define "Denominator":
	"Initial Population"

define "Numerator":
	"Male" and "IsMarried" and "IsType2Diabetic"

define "Male":
	Patient.gender.value = 'male'
	
define "IsMarried":
	Patient.maritalStatus in "Married"
	
define "IsType2Diabetic":
	exists([Condition : "Type2Diabetes"])