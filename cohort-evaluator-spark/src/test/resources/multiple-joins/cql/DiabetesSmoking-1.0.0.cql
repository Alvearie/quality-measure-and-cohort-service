library "DiabetesSmoking" version '1.0.0'

using "OMOP" version '5.2.2'
include "OMOPHelpers" version '1.0.0'

valueset "Diabetes": 'HCPCS' version '2020'

context person

define "DiabetesObservationCode":
  OMOPHelpers."StandardizedObservations" observations where
  observations in "Diabetes"

define "cohort":
  exists("DiabetesObservationCode")
