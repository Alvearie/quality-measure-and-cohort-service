library Test
include "FHIRHelpers" version '4.0.0' called FHIRHelpers
using FHIR

parameter MeasurementPeriod default Interval[
  @1900-01-01T00:00:00.0,
  @2021-01-01T00:00:00.0
)

define "ValidObservation":
  [Observation] E
    where E.effective in MeasurementPeriod

define "ValidObservation within 4 days":
  exists(ValidObservation E1
    with ValidObservation E2
       such that E2.effective 4 days or less after E1.effective
       or E1.effective 4 days or less after E2.effective
       )

define "ValidObservation not within 4 days":
  not exists(ValidObservation E1
    with ValidObservation E2
       such that E2.effective 4 days or less after E1.effective
       or E1.effective 4 days or less after E2.effective
       )
