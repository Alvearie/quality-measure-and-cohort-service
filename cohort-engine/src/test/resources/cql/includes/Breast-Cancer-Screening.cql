library "Breast-Cancer-Screening" version '1'
using FHIR version '4.0.0'
include "FHIRHelpers" version '4.0.0' called FHIRHelpers 
valueset "Breast Cancer VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.1434.1000.1095'
valueset "Bilateral Mastectomy VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1005'
valueset "Unilateral Mastectomy Left VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1133'
valueset "Unilateral Mastectomy Right VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1134'
valueset "Mammogram VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1182.380'
context Patient

define "Ages 40 to 75":
  AgeInYears() >= 40 and AgeInYears() <= 75

define "Female": 
  Patient.gender.value = 'female'

define "MeetsInclusionCriteria":
  "Ages 40 to 75"
  and "Female"

define "MeetsExclusionCriteria":
  "Breast Cancer"
  or "Bilateral Mastectomy"
  or "Unilateral mastectomy left and right."
  or "Recent Mammogram"

define "Unilateral mastectomy left and right.":
  "Unilateral Mastectomy Left"
  and "Unilateral Mastectomy Right"

define "InPopulation":
  "MeetsInclusionCriteria" and not "MeetsExclusionCriteria" 

define "Recommendation": 
  if "InPopulation" then ''
  else null
  

define "Rationale":
  if "InPopulation" then null
  else null

define "Errors":
  null


define function ObservationLookBack(ObsList List<Observation>, LookBack System.Quantity):
  ObsList O
    let LookBackInterval: Interval[Now() - LookBack, Now()]
    where (O.effective as FHIR.dateTime).value in LookBackInterval
      or (O.effective as FHIR.instant).value in LookBackInterval
      or PeriodToInterval(O.effective as FHIR.Period) overlaps LookBackInterval
      or O.issued.value in LookBackInterval

define function PeriodToInterval(period FHIR.Period):
  if period is null then
    null
  else
    Interval[period."start".value, period."end".value]
