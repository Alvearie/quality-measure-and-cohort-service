library "MeasureB" version '1.0.0'
using "CohortTest" version '0.0.1'

context B

define "cohort":
  exists("Passes all checks")

define "Passes all checks":
  [B] typeB where
    typeB.boolean is not null and typeB.boolean = true
    and typeB.integer is not null and typeB.integer < 0
    and typeB.decimal is not null and typeB.decimal > 2.0
    and typeB.string is not null and typeB.string != '~Missing'
    and typeB.date is not null and typeB.date after Date(2019, 1, 1)
    and typeB.datetime is not null and typeB.datetime after DateTime(2019, 1, 1, 0, 0, 0, 0)
