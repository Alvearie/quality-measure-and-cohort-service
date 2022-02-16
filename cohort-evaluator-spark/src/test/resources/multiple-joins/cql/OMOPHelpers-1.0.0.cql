library OMOPHelpers version '1.0.0'

using OMOP version '5.2.2'

define function ToCode(code OMOP.code):
    System.Code {
      code: code.concept_id,
      system: null,
      version: null,
      display: null
    }

define function ToCode(code Tuple { concept_id String}) returns System.Code : external
