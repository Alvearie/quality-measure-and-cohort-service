library "TestWithParams" version '1.0.0'
using "FHIR" version '4.0.0'

parameter MaxAge Integer
parameter Unused Integer default 10

context Patient

define "Female":
	Patient.gender.value = 'female' and AgeInYearsAt(@2020-01-01) <= MaxAge
define "Male":
	Patient.gender.value = 'male' 
define "ParamMaxAge":
	ToString(MaxAge)