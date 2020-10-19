library "Test" version '1.0.0'
using "FHIR" version '4.0.0'
include "FHIRHelpers" version '4.0.0' called FHIRHelpers
parameter MeasurementPeriod default Interval[
  @2006-07-01T00:00:00.0,
  @2006-12-31T00:00:00.0
)
context Patient
define "ValidEncounters":
  [Encounter] E
    where E.period overlaps MeasurementPeriod
define "ValidEncounters2":
  ValidEncounters E1
    with ValidEncounters E2
       such that start of E2.period 30 days or less after end of E1.period

define "E1Count":
  Count(ValidEncounters)
define "E2Count":
  Count(ValidEncounters2)