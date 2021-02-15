library "TestAdultMales" version '1.0.0'
using "FHIR" version '4.0.0'

<<<<<<< HEAD
=======
include "TestAdultMales2" version '1.0.0'
include "TestAdultMales3" version '1.0.0'

>>>>>>> Changes for returning evidence
parameter MeasurementPeriod Interval<DateTime>

context Patient

<<<<<<< HEAD
define "Over 18":
	AgeInYears() >= 18
	
define "Initial Population":
	"Over 18"

define "Denominator":
	"Initial Population"

define "Numerator":
	"Male" 

define "Male":
	Patient.gender.value = 'male'
=======
define "Initial Population":
	"TestAdultMales2"."Over 18"
	
define "Denominator":
	"Initial Population"
	
define "Numerator":
	"TestAdultMales3".Male
>>>>>>> Changes for returning evidence
