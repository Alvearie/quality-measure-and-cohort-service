library "EncounterContext" version '1.0.0'
using "FHIR" version '4.0.0'

context Encounter

define "ValidEncounters":
    [Encounter] e
	where e.status.value = 'finished'

define "ValidEncounterCount":
    Count("ValidEncounters")
