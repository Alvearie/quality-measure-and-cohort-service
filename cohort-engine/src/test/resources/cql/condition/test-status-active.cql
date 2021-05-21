library "Test" version '1.0.0'

using FHIR version '4.0.0'

include "FHIRHelpers" version '4.0.0' called FHIRHelpers

codesystem "CONDCLINSTATUS": 'http://terminology.hl7.org/CodeSystem/condition-clinical'

code "Condition Relapse code": 'relapse' from "CONDCLINSTATUS" display 'Relapse'
code "Condition Recurrence code": 'recurrence' from "CONDCLINSTATUS" display 'Recurrence'
code "Condition Active code": 'active' from "CONDCLINSTATUS" display 'Active'

concept "Condition Relapse": { "Condition Relapse code" } display 'Relapse'
concept "Condition Recurrence": { "Condition Recurrence code" } display 'Recurrence'
concept "Condition Active": { "Condition Active code" } display 'Active'

context Patient

define HasActiveCondition:
	exists(ActiveOrRecurring([Condition]))
	
define function ActiveOrRecurring(CondList List<Condition>):
  CondList C
    where C.clinicalStatus ~ "Condition Active"
      or C.clinicalStatus ~ "Condition Recurrence"
      or C.clinicalStatus ~ "Condition Relapse"
	
	