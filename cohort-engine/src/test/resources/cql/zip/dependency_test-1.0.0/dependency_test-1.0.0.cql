library "EMX153_InitPop" version '1.1.1'
using FHIR version '4.0.0'
include "ImportantDependency" version '1.1.1' called "ImportantDependency1.1.1"

define "DependentFemale":
  "ImportantDependency1.1.1".Female

define "DependentMale":
  "ImportantDependency1.1.1".Male

define "DependentOverTheHill":
  "ImportantDependency1.1.1".Overthehill