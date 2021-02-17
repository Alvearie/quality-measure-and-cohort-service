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
<<<<<<< HEAD
	"TestAdultMales3".Male
>>>>>>> Changes for returning evidence
=======
	"TestAdultMales3".Male and "Male" and "Male2" and "Male3" and "Male4" and "Male5" and "Male6" and "Male7" and "Male8" and "Male9" and "Male10" and "Male11" and "Male12" and "Male13" and "Male14" and "Male15" and "Male16" and "Male17" and "Male18" and "Male19" and "Male20" 
	
define "Male":
	Patient.gender.value = 'male'
	
define "Male2":
	Patient.gender.value = 'male'	
	
define "Male3":
	Patient.gender.value = 'male'
	
define "Male4":
	Patient.gender.value = 'male'
	
define "Male5":
	Patient.gender.value = 'male'
	
define "Male6":
	Patient.gender.value = 'male'
	
define "Male7":
	Patient.gender.value = 'male'
	
define "Male8":
	Patient.gender.value = 'male'
	
define "Male9":
	Patient.gender.value = 'male'
	
define "Male10":
	Patient.gender.value = 'male'
	
define "Male11":
	Patient.gender.value = 'male'
	
define "Male12":
	Patient.gender.value = 'male'
	
define "Male13":
	Patient.gender.value = 'male'
	
define "Male14":
	Patient.gender.value = 'male'
	
define "Male15":
	Patient.gender.value = 'male'
	
define "Male16":
	Patient.gender.value = 'male'
	
define "Male17":
	Patient.gender.value = 'male'
	
define "Male18":
	Patient.gender.value = 'male'
	
define "Male19":
	Patient.gender.value = 'male'
	
define "Male20":
	Patient.gender.value = 'male'
>>>>>>> In flight work
