# Measure endpoint (/evaluation)
## Description
This endpoint will take a patient and a measure and return the measure report for that patient evaluated against that measure
## Parameters

- patientId: A single patient intended to be run.
- measureId: The id of the measure to run. You do not need a measureId and an identifier, you only need one or the other.
- identifier: The identifier of the measure to run. If you provide this, you do not need a measureId but you do need a version.
- version: The version of the measure to run. This is only necessary if you're locating your measure with the identifier rather than the measureId.
- parameters: A place to specify any neccessary input parameters for your define. For example, if your define requires a measurement period this is where you would supply it.
- evidenceOptions: Two options for adding extra information to the returned measure report.
	- includeEvaluatedResources: Whether or not to include the resources that were evaluated to determine if patients succeed or fail in the measure report.
	- defineReturnOption: If this is set to ALL, will return add the define result to the report regaredless of the type. If set to BOOLEAN and the define returns a boolean type, the boolean will be added to the measure report. If it is set to BOOLEAN and the define return type is not BOOLEAN, nothing will be added to the measure report.
- expandValueSets: A flag that is used to control whether or not the terminology provider is used to expand ValueSet references or if the FHIR :in modifier is used during search requests. The FHIR :in modifier is supported in IBM FHIR 4.7.0 and above. The default behavior is to expand value sets using the terminology provider in order to cover the widest range of FHIR server functionality. A value of false can be used to improve search performance if terminology resources are available on the data server and it supports the :in modifier.
- searchPageSize: How many results to return per page. The default is 1000 which is the maximum allowed page size in IBM FHIR.
- measure: an application/zip attachment named 'measure' that contains the measure and library artifacts to be evaluated
## Output
Given a patientId and identifying information about a measure, return a measure report detailing that patient's position in the different positions 
## Additional Notes
- Valueset resources required for Measure evaluation must be loaded to the FHIR server in advance of an evaluation request.


# Cohort endpoint (/cohort-evaluation)
## Description
This endpoint will take a list of patients and a cohort definition and return the subset of patients that will be included in that cohort.
## Parameters
- entrypoint: The specific cql file that contains the define you plan to run. If you change the entrypoint to a different cql file, you will also need to ensure your defineToRun is defined in that file.
- defineToRun: This is the specific define within the specified cql that will be evaluated. Altering this parameter will change which define is specifically evaluated. NOTE: Currently, only boolean defines are supported.
- patientIds: A comma-separated list of patients that you intend to run. A single patient does not require trailing commas.
- loggingLevel: A flag for setting logging levels. Default is NA (no additional logs), or you may specify TRACE for additional logs.
- parameters: A place to specify any neccessary input parameters for your define. For example, if your define requires a measurement period this is where you would supply it.
- cql_definition: A zip file containing all the necessary cql files for 
## Output
Given a list of patients and a define, the return value will be the list of patients who passed the define, aka those patients who, when evaluated against the define, returned true.