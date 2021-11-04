library Parent version '1.0.0'
include "Child" version '1.0.0'

using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  LocalFunction(A) > 18 and Child.VowelOrConsonant(A) = 'Vowel' and Exists("Child"."EntryPoint")

define function LocalFunction(a CohortTest.A):
  CalculateAgeInYears(a.datetime_col)
