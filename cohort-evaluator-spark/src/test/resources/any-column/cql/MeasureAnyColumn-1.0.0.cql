library "MeasureAnyColumn" version '1.0.0'

using "CohortTest" version '0.0.1'
include "CohortHelpers" version '1.0.0'
include "AnyColumnChild" version '1.0.0'

codesystem "SNOMED": 'http://snomed.info/sct'
codesystem "OTHER": 'http://system.other'
code "MadeUpCode": 'cd1' from "SNOMED" display 'string1'
code "OtherCode": 'cdA' from "OTHER"

concept "MyConcept": {
	"MadeUpCode", "OtherCode"
} display 'MyConcept'

context A

define "cohort":
  exists("Passes all checks")

define "Passes all checks":
  [A] typeA where
    exists(CohortHelpers.AnyColumn(typeA, 'code_col') intersect "MyConcept".codes)

define "Passes child checks":
	Exists(AnyColumnChild.FilteredB) and Exists(AnyColumnChild.FilteredC)
