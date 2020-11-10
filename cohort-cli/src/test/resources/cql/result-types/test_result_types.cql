library "test_result_types" version '1.0.0'
using "FHIR" version '4.0.0'
context Patient
define "ReturnEnumeration":
	Patient.gender
define "ReturnCode":
	Patient.maritalStatus
define "ReturnBoolean":
	Patient.gender.value = 'male'
define "ReturnDate":
	Patient.birthDate
define "ReturnList":
	["Condition"]