library "GrandChild" version '1.0.0'

using "CohortTest" version '0.0.1'

codesystem "SNOMED": 'http://snomed.info/sct'
codesystem "OTHER": 'http://system.other'
code "MadeUpCode": 'cd1' from "SNOMED"
code "OtherCode": 'cdA' from "OTHER"

concept "MyConcept": {
	"MadeUpCode", "OtherCode"
} display 'MyConcept'

context Patient

define "cohort":
  Exists([A: code_col in "MyConcept".codes] a
    where a.boolean_col = true)
