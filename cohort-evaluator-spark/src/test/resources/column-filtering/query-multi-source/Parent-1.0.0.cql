library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  from
    [B] b, [D] d
    where b.integer > 1000
      and d.integer > 1000
