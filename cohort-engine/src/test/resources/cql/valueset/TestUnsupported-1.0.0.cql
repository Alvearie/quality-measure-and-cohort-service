library "TestUnsupported" version '1.0.0'

using "FHIR" version '4.0.0'
include "FHIRHelpers" version '4.0.0'

codesystem "SNOMED-CT:2021-03": 'urn:oid:SNOMED-CT-2021-03' version '2021-03'

valueset IncludesVersion: 'urn:oid:IncludesVersion' version '1.0.0'
valueset IncludesCodeSystems: 'urn:oid:IncludesCodeSystem' codesystems { "SNOMED-CT:2021-03" }
valueset IncludesBoth: 'urn:oid:IncludesBoth' version '1.0.0' codesystems { "SNOMED-CT:2021-03" }

context Patient

define UsesVersionVS:
	[Condition:IncludesVersion]
	
define UsesVersionVSInOperator:
	[Condition] c where c.code in IncludesVersion
	
define UsesCodeSystemsVS:
	[Condition:IncludesCodeSystems]
	
define UsesCodeSystemsVSInOperator:
	[Condition] c where c.code in IncludesCodeSystems	
	
define UsesBothVS:
	[Condition:IncludesBoth]
	
define UsesBothVSInOperator:
	[Condition] c where c.code in IncludesBoth