library Test
include "FHIRHelpers" version '4.0.0' called FHIRHelpers
using FHIR

valueset "Lab Tests During Pregnancy": '2.16.840.1.113883.3.464.1003.111.12.1007'

parameter MeasurementPeriod default Interval[DateTime(2013, 1, 1, 0, 0, 0, 0), DateTime(2021, 1, 1, 0, 0, 0, 0))

define "EncounterWithinPeriod":
	(["Encounter"] D where Interval[start of D."period", end of D."period"] overlaps MeasurementPeriod)

define "The Encounter NotFollowedByCondition":
	("EncounterWithinPeriod" E
	with ["Condition"] C such that C is not null
	and C."onset" after start of E."period"
	and C."onset" before end of E."period" + 30 days)

define "NotFollowedByCondition":
	not exists ("EncounterWithinPeriod" E
	with ["Condition"] C such that C is not null
	and C."onset" after start of E."period"
	and C."onset" before end of E."period" + 30 days)