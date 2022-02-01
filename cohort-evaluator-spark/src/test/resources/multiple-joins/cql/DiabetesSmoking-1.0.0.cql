library "DiabetesSmoking" version '1.0.0'

using "OMOP" version '5.2.2'

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

define function ToCode(concept concept, vocabulary vocabulary):
    System.Code {
      code: concept.concept_code,
      system: concept.vocabulary_id,
      version: vocabulary.vocabulary_version,
      display: concept.concept_name
    }

define "cohort":
  exists("DiabetesObservationValueSet")
