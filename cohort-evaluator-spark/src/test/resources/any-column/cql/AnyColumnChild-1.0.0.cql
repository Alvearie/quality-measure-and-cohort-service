library "AnyColumnChild" version '1.0.0'

using "CohortTest" version '0.0.1'
include "CohortHelpers" version '1.0.0'
include "AnyColumnGrandChild" version '1.0.1'

context A

define "FilteredB":
  [B] b where Exists(CohortHelpers.AnyColumnRegex(b, 'integer_col') intersect { 123 })

define "FilteredC":
  "AnyColumnGrandChild".FilteredC

define "Top":
  [A] topA
  let M: singleton from ("Middle") where M.pat_id = topA.pat_id

define "Middle":
  [A] midA
    with [C] midC
    such that midC.big_decimal > midA.decimal_col

define "Bottom":
  [C] botC
    return Tuple { da: A.decimal_col, dc: botC.big_decimal }

define "Renamed":
  "Bottom" bot
  where bot.da > 10.10
  sort by da

define "Aggregation":
  [C] aggC
    return aggC.double

define "Counted":
  Count([C] c where c.double < 0 )

define "LetUsingExprRef":
  [A] a
     let mid: "Middle" m where m.decimal_col < 0

define function VowelOrConsonant(a CohortTest.A):
  if Matches(a.string_col, '.*[AaEeIiOoUu].*') then 'Vowel' else 'Consonant'
