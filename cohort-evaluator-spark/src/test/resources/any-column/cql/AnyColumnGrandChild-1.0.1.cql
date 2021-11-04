library "AnyColumnGrandChild" version '1.0.1'

using "CohortTest" version '0.0.1'
include "CohortHelpers" version '1.0.0'

define "FilteredC":
  [A] a
    with [C] c
    such that c.big_decimal > a.decimal_col
          and Exists(CohortHelpers.AnyColumnRegex(c, '.*_decimal') intersect { 123 })
