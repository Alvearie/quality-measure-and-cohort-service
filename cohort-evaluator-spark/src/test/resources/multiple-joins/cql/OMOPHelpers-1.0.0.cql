library OMOPHelpers version '1.0.0'

using OMOP version '5.2.2'

define function ToCode(observation OMOP.observation):
    System.Code {
      code: observation.observation_concept_id,
      system: null,
      version: null,
      display: null
    }

define function ToCode(concept concept, vocabulary vocabulary):
    System.Code {
      code: concept.concept_code,
      system: concept.vocabulary_id,
      version: vocabulary.vocabulary_version,
      display: concept.concept_name
    }
