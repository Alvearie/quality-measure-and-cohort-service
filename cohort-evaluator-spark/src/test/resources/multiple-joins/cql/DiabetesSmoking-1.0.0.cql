library "DiabetesSmoking" version '1.0.0'

using "OMOP" version '5.2.2'
include "OMOPHelpers" version '1.0.0'

valueset "Diabetes": 'HCPCS' version '2020'

context person

define "DiabetesObservation":
  [observation] o
  where o.observation_concept_id in { 2617212, 2617211 }

define "DiabetesObservationValueSet":
  from [observation] o
    , [concept] c
    , [vocabulary] v
  where
    o.observation_concept_id = c.concept_id
    and c.vocabulary_id = v.vocabulary_id
    and ToCode(c, v) in "Diabetes"

define: "DiabetesObservationCode":
  [observation] o where o.code != null

define "cohort":
  exists("DiabetesObservationCode")
