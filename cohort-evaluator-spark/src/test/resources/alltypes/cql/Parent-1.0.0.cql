library "Parent" version '1.0.0'

using "CohortTest" version '0.0.1'
include "Child" version '1.0.0'

context Patient

define "cohort":
  Child.cohort
