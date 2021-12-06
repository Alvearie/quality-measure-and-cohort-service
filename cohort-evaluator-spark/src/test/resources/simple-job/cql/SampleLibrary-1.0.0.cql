library "SampleLibrary" version '1.0.0'
include "CohortHelpers" version '1.0.0'
using MOCK version '1.0.0'

context Patient

define IsFemale:
	Patient.gender = 'female'

define getNow:
    Now()