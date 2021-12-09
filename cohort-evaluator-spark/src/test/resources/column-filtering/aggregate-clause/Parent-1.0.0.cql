library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  [B] b
    aggregate Result starting 0: Result + b.integer
