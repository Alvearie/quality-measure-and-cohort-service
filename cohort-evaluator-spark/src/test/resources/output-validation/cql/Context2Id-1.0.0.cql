library "Context1String" version '1.0.0'
using "CohortTest" version '0.0.1'

context Context2Id
  
define "define_date":
  if Context2Id.boolean then Context2Id.date else null
  
define "define_datetime":
  if Context2Id.boolean then Context2Id.datetime else null
