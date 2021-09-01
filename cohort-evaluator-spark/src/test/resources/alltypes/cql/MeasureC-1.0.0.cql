library "MeasureC" version '1.0.0'
using "CohortTest" version '0.0.1'

context C

define "cohort":
  exists("Passes all checks")

define "Passes all checks":
  [C] typeC where
    typeC.big_decimal is not null and typeC.big_decimal >= 2.5
    and typeC.double is not null and typeC.double >= 2.5
    and typeC.float is not null and typeC.float >= 2.5
