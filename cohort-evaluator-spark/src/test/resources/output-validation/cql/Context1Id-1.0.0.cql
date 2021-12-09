library "Context1Id" version '1.0.0'
using "CohortTest" version '0.0.1'

context Context1Id

define "define_integer":
  if Context1Id.boolean then Context1Id.integer else null
  
define "define_decimal":
  if Context1Id.boolean then Context1Id.decimal else null  

define "define_string":
  if Context1Id.boolean then Context1Id.string else null

define "define_boolean":
  Context1Id.boolean