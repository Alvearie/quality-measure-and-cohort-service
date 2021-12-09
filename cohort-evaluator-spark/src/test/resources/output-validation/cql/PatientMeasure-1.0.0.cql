library "PatientMeasure" version '1.0.0'
using "CohortTest" version '0.0.1'

context Patient
  
define "cohort":
  exists([Type1] t1 where t1.boolean) and exists([Type2] t2 where t2.boolean)
