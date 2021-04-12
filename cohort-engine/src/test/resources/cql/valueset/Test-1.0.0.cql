library "Test" version '1.0.0'

using "FHIR" version '4.0.0'
include "FHIRHelpers" version '4.0.0'

valueset "TestExists": 'https://cts.nlm.nih.gov/fhir/ValueSet/1.2.3.4'
valueset "TestNotExists": 'https://cts.nlm.nih.gov/fhir/ValueSet/5.6.7.8'


context Patient

define RHSRetrieveMatches:
	exists([Condition: TestExists])
	
define RHSInOperatorMatches:
	exists([Condition] c where c.code in TestExists)
	
define RHSRetrieveNotMatches:
	not exists([Condition: TestNotExists])
	
define RHSInOperatorNotMatches:
	not exists([Condition: TestNotExists])