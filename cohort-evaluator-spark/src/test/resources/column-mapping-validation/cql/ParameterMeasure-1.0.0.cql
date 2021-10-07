library "ParameterMeasure" version '1.0.0'
using "CohortTest" version '0.0.1'

parameter NumberToCheck Integer

context Patient
  
define "cohort":
  NumberToCheck
