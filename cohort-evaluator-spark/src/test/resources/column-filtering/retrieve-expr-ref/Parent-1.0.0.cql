library Parent version '1.0.0'
using "CohortTest" version '0.0.1'

valueset MyValueSet: 'urn:oid:123'

context A

define EntryPoint:
  "WithWhereClause"

define WithWhereClause:
  "WithCodeFilter" cf where cf.boolean_col = false

define WithCodeFilter:
  [A: MyValueSet]
