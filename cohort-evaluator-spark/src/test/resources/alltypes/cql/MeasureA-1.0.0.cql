library "MeasureA" version '1.0.0'
using "CohortTest" version '0.0.1'

codesystem "SNOMED": 'http://snomed.info/sct'
codesystem "OTHER": 'http://system.other'
code "MadeUpCode": 'cd1' from "SNOMED"
code "OtherCode": 'cdA' from "OTHER"

concept "MyConcept": {
	"MadeUpCode", "OtherCode"
} display 'MyConcept'

context A

define "cohort":
  exists("Passes all checks")

define "Passes all checks":
  [A: code_col in "MyConcept"] typeA where
    typeA.boolean_col is not null and typeA.boolean_col = true
    and typeA.integer_col is not null and typeA.integer_col < 21
    and typeA.decimal_col is not null and typeA.decimal_col > 0.5
    and typeA.string_col is not null and typeA.string_col in {'string1', 'string2', 'default'}
    and typeA.date_col is not null and typeA.date_col after Date(1999, 12, 31)
    and typeA.datetime_col is not null and typeA.datetime_col after DateTime(1999, 1, 1, 0, 0, 0, 0)
    and typeA.code_col2 is not null and typeA.code_col2 ~ "OtherCode"
    and typeA.decimal_col is not null and 1 < typeA.decimal_col
