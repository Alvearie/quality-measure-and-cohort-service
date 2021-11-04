library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  (
    ("RetrieveB" b return b.integer)
    union
    ("RetrieveD" d return WholeNumbers(d))
  ) numbers
  where Sum(numbers) > 100

define RetrieveB:
  [B]

define RetrieveD:
  [D]

define function WholeNumbers(obj CohortTest.D):
  List<System.Long> { obj.short, obj.integer, obj.long }
