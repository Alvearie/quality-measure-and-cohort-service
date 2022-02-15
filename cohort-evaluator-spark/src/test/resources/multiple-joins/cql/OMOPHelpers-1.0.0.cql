library OMOPHelpers version '1.0.0'

using OMOP version '5.2.2'

define "StandardizedObservations":
  from [observation] o
    , [concept] c
    , [vocabulary] v
  where
    o.observation_concept_id = c.concept_id
    and c.vocabulary_id = v.vocabulary_id
  return convert { observation: o, concept: c, vocabulary: v } to OMOP.coding

define function ToCode(coding OMOP.coding):
    System.Code {
      code: coding.concept.concept_code,
      system: coding.concept.vocabulary_id,
      version: coding.vocabulary.vocabulary_version,
      display: coding.concept.concept_name
    }

// coding.observation.observation_concept_id as string
