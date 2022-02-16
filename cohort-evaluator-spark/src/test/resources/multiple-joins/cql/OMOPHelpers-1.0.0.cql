library OMOPHelpers version '1.0.0'

using OMOP version '5.2.2'

define "StandardizedObservationTuple":
  from [observation] o
    , [concept] c
    , [vocabulary] v
  where
    o.observation_concept_id = c.concept_id
    and c.vocabulary_id = v.vocabulary_id

define "StandardizedObservations":
  "StandardizedObservationTuple" tuple
  return System.Code { code: tuple.c.concept_code, system: tuple.c.vocabulary_id, version: tuple.v.vocabulary_version, display: tuple.c.concept_name }

define "StandardizedObservations2":
  from [observation] o
    , [concept] c
    , [vocabulary] v
  where
    o.observation_concept_id = c.concept_id
    and c.vocabulary_id = v.vocabulary_id
  return all convert Tuple { observation: o, concept: c, vocabulary: v } to OMOP.coding

//   return OMOP.coding { observation: o, concept: c, vocabulary: v }
//  return all { observation: o, concept: c, vocabulary: v } to OMOP.coding

define function ToCode(coding OMOP.coding):
    System.Code {
      code: coding.concept.concept_code,
      system: coding.concept.vocabulary_id,
      version: coding.vocabulary.vocabulary_version,
      display: coding.concept.concept_name
    }

// coding.observation.observation_concept_id as string
