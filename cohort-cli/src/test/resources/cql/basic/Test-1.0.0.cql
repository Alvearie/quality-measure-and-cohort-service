library "Test" version '1.0.0'
using "FHIR" version '4.0.0'
context Patient
define "Female":
	Patient.gender.value = 'female'
define "Male":
	Patient.gender.value = 'male'
define "Over the hill":
	AgeInYears() >= 40