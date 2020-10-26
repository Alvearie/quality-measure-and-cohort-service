library "Test" version '1.0.0'
using "FHIR" version '4.0.0'

parameter ValueSetOverride String default 'urn:oid:2.16.840.1.113883.3.464.1003.102.12.1011'

valueset "Customer Can Override": "ValueSetOverride"

context Patient

define "ValueSet":
	"Customer Can Override"