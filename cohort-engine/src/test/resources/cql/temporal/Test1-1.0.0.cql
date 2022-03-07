library "Test1" version '1.0.0'
using "FHIR" version '4.0.0'
include "FHIRHelpers" version '4.0.0' called FHIRHelpers
parameter MeasurementPeriod default Interval[
  @1900-01-01T00:00:00.0,
  @2021-01-01T00:00:00.0
)
context Patient
define "ValidEncounters":
  [Encounter] E
    where E.period overlaps MeasurementPeriod
define "Conditions":
  [Condition] C
    with ValidEncounters E
       such that C.onset 1 years or less after end of E.period
define "Observations":
  [Observation] O
    with Conditions C
      such that O.effective 1 years or less  after C.abatement
define "Observations Exist":
    exists(Observations)
define "E1Count":
  Count(ValidEncounters)