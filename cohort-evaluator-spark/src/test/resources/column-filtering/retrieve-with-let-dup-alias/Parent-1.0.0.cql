library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  [B] b
    let d: singleton from (
        [D] d where d.integer mod 10 = 0
    )
    where d.long > 1000
