library "Child" version '1.0.0'

using "CohortTest" version '0.0.1'
include "GrandChild" version '1.0.0'

define "cohort":
  "GrandChild"."cohort"
