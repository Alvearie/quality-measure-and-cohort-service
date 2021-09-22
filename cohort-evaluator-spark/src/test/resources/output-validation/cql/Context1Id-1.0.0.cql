library "Context1Id" version '1.0.0'
using "CohortTest" version '0.0.1'

context Context1Id

define "define_integer":
  if exists([Type1] t where t.boolean) then 1 else 0
  
define "define_decimal":
  if exists([Type1] t where t.boolean) then 1.0 else 0.0
  
define "define_string":
  if exists([Type1] t where t.boolean) then '1' else '0'

define "define_boolean":
  exists([Type1] t where t.boolean)
