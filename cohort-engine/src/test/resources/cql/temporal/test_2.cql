library Test
include "FHIRHelpers" version '4.0.0' called FHIRHelpers
using FHIR
parameter MeasurementPeriod default Interval[
  @1900-01-01T00:00:00.0,
  @2021-01-01T00:00:00.0
)

define "EncounterWithinPeriod":
	(["Encounter"] D where Interval[start of D."period", end of D."period"] overlaps MeasurementPeriod)

define "EncounterWith30daySeparationFromCondition":
	"EncounterWithinPeriod" E
	with ["Condition"] C such that C is not null
	and C."onset" after start of E."period"
	and C."onset" before end of E."period" + 30 days

define "ObservationWithin30DaysOfCondition":
	exists (
	"EncounterWith30daySeparationFromCondition" E with ["Observation"] O such that O is not null
	and O."effective" after start of E."period"
	and O."effective" before end of E."period" + 10 days
	)