##### IHE
Intelligent Health Engagement team, formerly know as Digital Clinical Conversation (DCC)

##### DCC
?> _deprecated:_ see `IHE`

##### JXSON
!> _crispy warning:_ not an official JSON serialization format or term.

Term coined by the `cql_engine` team to indicate the Jackson flavor of JSON serialization.

##### Bonnie
An electronic Clinical Quality Measure (eCQM) tool for testing electronic quality measures

- https://bonnie.healthit.gov/users/sign_in
- https://bonnie.ahrqstg.org/
- https://github.com/projecttacoma/bonnie

##### Bonnie API
Provides API access to Measure resources defined in MAT

https://bonnie.healthit.gov/api

##### CMS
Centers for Medicare and Medicaid Services

##### eCQI 
Electronic Clinical Quality Improvement

Funded by CMS and ONC, Electronic clinical quality improvement (eCQI) provides common standards and shared technologies to monitor and analyze the quality of health care provided to patients and patient outcomes.

https://ecqi.healthit.gov/

##### DSTU 
Draft Standard for Trial Use 

##### ELM 
Expression Logical Model

https://cql.hl7.org/elm.html

##### FSH 
FHIR Shorthand

https://build.fhir.org/ig/HL7/fhir-shorthand/index.html

##### HEDIS
Healthcare Effectiveness Data and Information Set

https://www.ncqa.org/programs/data-and-information-technology/hit-and-data-certification/hedis-compliance-audit-certification/

##### HCIS
Healthcare Information Services 

Partnership between Net-Integrated Consulting and Telegen to maintain the Measure Authoring Tool for CMS

##### HQMF
Health Quality Measure Format

A standards-based representation of quality measures as electronic documents written in XML.

https://ecqi.healthit.gov/hqmf

##### MAT
Measurement Author Tool, Created by CMS for use in creating eCQM measurement definitions

- https://www.emeasuretool.cms.gov
- https://github.com/MeasureAuthoringTool

##### NQF
National Quality Forum

https://www.qualityforum.org/Measuring_Performance/Measuring_Performance.aspx

Consensus-based healthcare organization that provides definitions and library of measurements for nationally acceptable measures of quality

##### QDM
Quality Data Model

https://ecqi.healthit.gov/qdm

An information model that defines relationships between patients and clinical concepts in a standardized format to enable electronic quality performance measurement. 
<csanders> This is the legacy data model for CMS eCQM work. FHIR is another possible data model. FHIR provides a mapping spec that maps from QDM to US Core 4, FHIR 4.0 http://hl7.org/fhir/us/qicore/qdm-to-qicore.html

##### QUICK
The QUICK data model provides a logical view of clinical data from the perspective of representing quality measurement and decision support knowledge.

http://hl7.org/fhir/us/qicore/2018Jan/quick/index.html

The QUICK data model uses the QI-Core profiles to provide a physical representation for the data. 
QUICK provides a logical model that enables knowledge authors to ignore certain details of the FHIR Physical representation, including:
- The representation of primitives in FHIR using a "value" element of a complex type, rather than a true primitive
- The representation of extensions in FHIR as first class elements in QUICK
- Direct reference of resources, rather than needing to traverse a "reference"

To address the first issue, the QUICK model maps the FHIR base types to CQL primitives, rather than using the FHIR types directly:

To address the second issue, the QUICK model represents FHIR extensions as first-class attributes of the class. 

To address the third issue, the QUICK model represents FHIR references as direct appearances of the referenced class or classes. 

!> The third issue is still being worked out, so current QUICK documentation still uses the Reference type to model references.

##### STU
Standard for Trial Use

https://confluence.hl7.org/display/HL7/HL7+Balloting

Standard for Trial Use (STU) ballots are used to vet content that is eventually intended to be binding on implementers.

##### TREX 
is a data integration component that advantage suite (aka Truven) offerings use for data ingestion into their pipeline.
We are now decoupling it though as a component that has value for claims data integration for more than just Advantage offerings

##### VSAC
Value Set Authority Center

https://vsac.nlm.nih.gov/

The VSAC is a repository and authoring tool for public value sets created by external programs. Value sets are lists of codes and corresponding terms, from NLM-hosted standard clinical vocabularies (such as SNOMED CT®, RxNorm, LOINC® and others), that define clinical concepts to support effective and interoperable health information exchange.

##### VSAC API
The FHIR Terminology Service for VSAC Resources is a RESTful API service for accessing the VSAC value sets and supported code systems.

- https://cts.nlm.nih.gov/fhir/
- https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
