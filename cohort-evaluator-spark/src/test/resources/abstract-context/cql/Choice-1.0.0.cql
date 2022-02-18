library "Choice" version '1.0.0'
using "Abstract" version '1.0.0'

context AlphaNumeric

define "validNumeric":
    [AlphaNumeric] an where
    an.id > 1
    and an.unit = 'mg'

define "cohort":
	exists("validNumeric")
