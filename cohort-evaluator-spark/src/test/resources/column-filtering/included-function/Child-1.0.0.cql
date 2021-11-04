library Child version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  [B] b
    with [C] c
    such that b.decimal < c.big_decimal

define function VowelOrConsonant(a CohortTest.A):
  if Matches(a.string_col, '.*[AaEeIiOoUu].*') then 'Vowel' else 'Consonant'
