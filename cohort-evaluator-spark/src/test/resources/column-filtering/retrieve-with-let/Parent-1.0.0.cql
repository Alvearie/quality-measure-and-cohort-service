library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  [B] b
    let age: CalculateAgeInYears(b.datetime), distance: b.integer
    with [D] d
    such that d.integer > age
    without [C] c
    such that c.float > distance
    where b.boolean = false
    sort by id
