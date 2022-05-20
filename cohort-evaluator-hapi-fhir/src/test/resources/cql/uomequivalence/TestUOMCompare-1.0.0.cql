library "TestUOMCompare" version '1.0.0'

parameter ValInGrams default 10 'g'
parameter ValInMilligrams default 10000 'mg'

define IsEqual:
	ValInGrams = ValInMilligrams

define AreEquivalent:
	ValInGrams = convert ValInMilligrams to 'g'

define UpConvert:
	convert ValInGrams to 'kg' = convert ValInMilligrams to 'kg'