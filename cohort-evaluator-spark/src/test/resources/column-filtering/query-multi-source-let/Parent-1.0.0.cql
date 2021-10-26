library Parent version '1.0.0'
using "CohortTest" version '0.0.1'
context A

define EntryPoint:
  from
    "RetrieveB" b, "RetrieveD" d
    let combined: b.integer + d.long
  where combined < DoMultiplication(b.decimal, DoDivision(d.short,2))

define RetrieveB:
  [B]

define RetrieveD:
  [D]

define function DoMultiplication(val System.Decimal, multiplier System.Decimal):
  val * multiplier

define function DoDivision(val System.Decimal, divider System.Decimal):
  if divider != 0 then val / divider else 0
