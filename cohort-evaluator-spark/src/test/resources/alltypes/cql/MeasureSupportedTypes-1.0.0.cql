library "MeasureSupportedTypes" version '1.0.0'
using "CohortTest" version '0.0.1'

define "intDefine":
  if Count([A]) > 1 then 1 else 0
  
define "decimalDefine":
  if Count([A]) > 1 then 1.0 else 0.0
  
define "stringDefine":
  if Count([A]) > 1 then '1' else '0'
  
define "booleanDefine":
  if Count([A]) > 1 then true else false
  
define "dateDefine":
  if Count([A]) > 1 then @2020-01-01 else @2019-01-01
  
define "datetimeDefine":
  if Count([A]) > 1 then @2020-01-01T00:00:00.0Z else @2019-01-01T00:00:00.0Z
