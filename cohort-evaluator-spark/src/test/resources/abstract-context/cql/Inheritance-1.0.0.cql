library "Inheritance" version '1.0.0'
using "Abstract" version '1.0.0'

context Alpha

define "validLetterA":
    [Alpha] a where
    a.id > 1
    and a.index = 1

define "cohort":
	exists("validLetterA")
