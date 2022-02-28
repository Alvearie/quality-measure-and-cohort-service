library "BreastCancerScreening" version '1.0.0'
using FHIR version '4.0.0'
include "FHIRHelpers" version '4.0.0' called FHIRHelpers 
codesystem "CONDCLINSTATUS": 'http://terminology.hl7.org/CodeSystem/condition-clinical'
valueset "Breast Cancer VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.1434.1000.1095'
valueset "Bilateral Mastectomy VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1005'
valueset "Unilateral Mastectomy Left VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1133'
valueset "Unilateral Mastectomy Right VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1134'
valueset "Mammogram VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1182.380'
code "Condition Relapse code": 'relapse' from "CONDCLINSTATUS" display 'Relapse'
code "Condition Recurrence code": 'recurrence' from "CONDCLINSTATUS" display 'Recurrence'
code "Condition Active code": 'active' from "CONDCLINSTATUS" display 'Active'
concept "Condition Relapse": { "Condition Relapse code" } display 'Relapse'
concept "Condition Recurrence": { "Condition Recurrence code" } display 'Recurrence'
concept "Condition Active": { "Condition Active code" } display 'Active'
context Patient

define "Female": 
  Patient.gender.value = 'female'

define "40-65 years of age":
  AgeInYears() >= 40 and AgeInYears() <= 65

define "Breast Cancer":
  exists(ActiveOrRecurring([Condition: "Breast Cancer VS"]))

define "Bilateral Mastectomy":
  exists([Procedure: "Bilateral Mastectomy VS"])

define "Unilateral Mastectomy Left":
  exists([Procedure: "Unilateral Mastectomy Left VS"])

define "Unilateral Mastectomy Right":
  exists([Procedure: "Unilateral Mastectomy Right VS"])

define "Mammogram":
  exists(ObservationLookBack([Observation: "Mammogram VS"], 12 months))

define "MeetsInclusionCriteria":
  "Female"
  and "40-65 years of age"

define "MeetsExclusionCriteria":
  "Breast Cancer"
  or "Bilateral or two unilateral mastectomies."
  or "Mammogram"

define "Bilateral or two unilateral mastectomies.":
  "Bilateral Mastectomy"
  or "Left and Right Mastectomy"

define "Left and Right Mastectomy":
  "Unilateral Mastectomy Left"
  and "Unilateral Mastectomy Right"

define "InPopulation":
  "MeetsInclusionCriteria" and not "MeetsExclusionCriteria" 

define "Recommendation": 
  null

define "Rationale":
  null

define "Errors":
  null


define function ObservationLookBack(ObsList List<Observation>, LookBack System.Quantity):
  ObsList O
    let LookBackInterval: Interval[Now() - LookBack, Now()]
    where (O.effective as FHIR.dateTime).value in LookBackInterval
      or (O.effective as FHIR.instant).value in LookBackInterval
      or PeriodToInterval(O.effective as FHIR.Period) overlaps LookBackInterval
      or O.issued.value in LookBackInterval

define function ActiveOrRecurring(CondList List<Condition>):
  CondList C
    where C.clinicalStatus ~ "Condition Active"
      or C.clinicalStatus ~ "Condition Recurrence"
      or C.clinicalStatus ~ "Condition Relapse"

define function PeriodToInterval(period FHIR.Period):
  if period is null then
    null
  else
    Interval[period."start".value, period."end".value]
