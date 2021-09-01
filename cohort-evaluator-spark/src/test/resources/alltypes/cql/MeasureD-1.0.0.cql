library "MeasureD" version '1.0.0'
using "CohortTest" version '0.0.1'

context D

define "cohort":
  exists("Passes all checks")

define "Passes all checks":
  [D] typeD where
    typeD.integer is not null and typeD.integer = 1
    and typeD.long is not null and typeD.long = 2
    and typeD.short is not null and typeD.short > 0
