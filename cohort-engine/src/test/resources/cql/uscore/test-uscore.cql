library "Test" version '1.0.0'

using "USCore" version '3.1.0'

context Patient

define QueryByGender:
	["PatientProfile"] p where p.gender = 'M'