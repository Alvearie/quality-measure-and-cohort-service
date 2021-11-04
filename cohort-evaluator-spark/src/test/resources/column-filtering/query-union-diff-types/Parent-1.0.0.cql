library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  CombineTypes c where StartsWith(c.id, 'A')

define CombineTypes:
  (
    "RetrieveB"
    union
    "RetrieveD"
  ) numbers
  where IntValue(numbers) > 100

define RetrieveB:
  [B]

define RetrieveD:
  [D]

define function IntValue(obj Choice<CohortTest.B,CohortTest.D>):
  obj.integer
