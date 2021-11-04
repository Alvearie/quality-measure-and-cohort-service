library Parent version '1.0.0'
using "CohortTest" version '0.0.1'

codesystem "SNOMED": 'http://snomed.info/sct'
codesystem "OTHER": 'http://system.other'
code "MadeUpCode": 'cd1' from "SNOMED" display 'string1'
code "OtherCode": 'cdA' from "OTHER"

concept "MyConcept": {
	"MadeUpCode", "OtherCode"
} display 'MyConcept'

context A

define EntryPoint:
  MyQuantity.value + MyRatio.numerator.value +
    CalculateAgeInYearsAt(A.datetime_col, MyInterval.low) +
    Count(MyConcept.codes) + MyTuple.age

define MyQuantity:
  25 'mg/mL'

define MyRatio:
  100 'mg' : 1000 'mL'

define MyInterval:
  Interval[@2000-01-01,@2010-07-04)

define MyTuple:
  Tuple { "age": 109 }
