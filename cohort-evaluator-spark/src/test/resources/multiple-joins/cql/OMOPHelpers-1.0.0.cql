library OMOPHelpers version '1.0.0'

using OMOP version '5.2.2'

define "StandardizedObservations":
  from [observation] o
    , [concept] c
    , [vocabulary] v
  where
    o.observation_concept_id = c.concept_id
    and c.vocabulary_id = v.vocabulary_id

define function ToCode(coding Tuple { observation OMOP.observation, concept OMOP.concept, vocabulary OMOP.vocabulary }):
    System.Code {
      code: null,
      system: null,
      version: null,
      display: null
    }

// coding.observation.observation_concept_id as string