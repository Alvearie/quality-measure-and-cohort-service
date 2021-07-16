library "Dependency Test" version '1.0.0'
using FHIR version '4.0.0'
include "ImportantDependency" version '1.1.1' called "ImportantDependency1.1.1"
context Patient

define "DependentFemale":
  "ImportantDependency1.1.1".Female

define "DependentMale":
  "ImportantDependency1.1.1".Male

define "DependentOverTheHill":
  "ImportantDependency1.1.1".Overthehill