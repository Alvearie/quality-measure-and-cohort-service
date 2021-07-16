library "QualifyingEncounters" version '1.1.1'
using FHIR version '4.0.0'

define "Female":
	Patient.gender.value = 'female'
define "Male":
	Patient.gender.value = 'male'
define "Overthehill":
	AgeInYears() >= 40