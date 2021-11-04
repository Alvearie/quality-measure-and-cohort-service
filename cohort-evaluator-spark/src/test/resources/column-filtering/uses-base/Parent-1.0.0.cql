library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  Sum([B] b return Amount(b)) + Sum([C] c return Amount(c))
    

define function Amount(obj CohortTest.ChildBase):
  obj.amount