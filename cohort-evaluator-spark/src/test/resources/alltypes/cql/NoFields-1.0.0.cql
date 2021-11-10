library "NoFields" version '1.0.0'
using "CohortTest" version '0.0.1'

context Patient
  
define "cohort":
  exists([B]) and exists([C])