library "MeasureAB" version '1.0.0'
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
  exists("Passes A checks")
  and exists("Passes B checks")

define "Passes A checks":
  [A: code_col in "MyConcept".codes] typeA where
    typeA.boolean_col is not null and typeA.boolean_col = true

define "Passes B checks":
  [B] typeB where
    typeB.date is not null and typeB.date after Date(2019)
    and typeB.decimal is not null and typeB.decimal >= 5.9