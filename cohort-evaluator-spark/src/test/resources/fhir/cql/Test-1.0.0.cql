library "Test" version '1.0.0'
using FHIR version '4.0.1'
include "FHIRHelpers" version '4.0.1'
context Patient

define WithJoin:
  [Encounter] e
   with [Observation] o
    such that o.effective as FHIR.dateTime during e.period
   with [Condition] c
    such that c.id in e.diagnosis.condition.reference

define RawRetrieve:
  [Encounter]

define Sorted:
  [Condition] c
    sort by id

define InTuple:
    Tuple { dt1 : Patient.birthDate, dt2: Patient.birthDate }

define Counted:
  Count(Patient.name)

define MultipleLet:
  Patient p
    let n1: p.name[0], n2: p.name[1]
    return Tuple {"n1": n1, "n2": n2} 
