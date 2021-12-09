library "ThrowsException" version '1.0.0'
using "CohortTest" version '0.0.1'

context Patient
  
define "cohort":
  IsNull( singleton from ( [A] ) ) 