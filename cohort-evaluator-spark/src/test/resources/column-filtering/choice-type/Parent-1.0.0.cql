library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  MySum([B]) + MySum([D])

define function MySum(elements List<Choice<CohortTest.B,CohortTest.D>>):
  Sum(elements E return all E.integer)
