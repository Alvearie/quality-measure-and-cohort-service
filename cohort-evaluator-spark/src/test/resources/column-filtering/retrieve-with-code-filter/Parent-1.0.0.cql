library Parent version '1.0.0'
using "CohortTest" version '0.0.1'

valueset MyValueSet: 'urn:oid:123'

context A

define EntryPoint:
  Exists(UsesDefaultCodePath) and Exists(UsesAlternateCodePath)

define UsesDefaultCodePath:
  [A: MyValueSet]

define UsesAlternateCodePath:
  [A: code_col2 in MyValueSet]
