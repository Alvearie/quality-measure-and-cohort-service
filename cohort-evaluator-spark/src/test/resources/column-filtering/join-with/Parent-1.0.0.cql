library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  [B] b
    with [C] c
    such that b.decimal < c.big_decimal
