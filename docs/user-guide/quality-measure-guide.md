
# FHIR Clinical Quality Measure Evaluation

The [Quality Reporting](http://www.hl7.org/fhir/clinicalreasoning-quality-reporting.html) section of the [FHIR Clinical Reasoning Module](http://www.hl7.org/fhir/clinicalreasoning-module.html) describes a Measure resource that that can be used to capture the metadata and struction information that is specific to clinical quality measures. The cohort-and-quality-measure service provides interfaces that can be used to evaluate clinical quality measures described by these FHIR Measure resoures in the client-server and server-only mode of operation. Similar to the cohort evaluator function, the quality measure evaluator can be interacted with using a provided command-line interface, via the proprietary cohort-services REST API, or embedded directly in a consuming application via provided Java APIs.

## Command-line interface

The command-line interface for the quality measure evaluator is very similar to the command-line-interface of the cohort evaluator. If you have not already done so, it would be helpful to review the details of the [cohort-evaluator command-line interface](user-guide/client-server-guide.md#command-line-interface) before continuing with this guide.

```
$ java -classpath cohort-cli/target/cohort-cli-VERSION-shaded.jar com.ibm.cohort.cli.MeasureCLI --help
Usage: measure-engine [options]
  Options:
  * -c, --context-id
      FHIR resource ID for one or more patients to evaluate.
  * -d, --data-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve data.
    -o, --define-return-option
      Specify define return option for evaluated define statements on measure
      report. Defaults to NONE. To view returned results, must specify -f
      JSON.
      Default: NONE
      Possible Values: [ALL, BOOLEAN, NONE]
    --disable-retrieve-cache
      Disable the use of the retrieve cache.
      Default: false
    --enable-terminology-optimization
      By default, ValueSet resources used in CQL are first expanded by the
      terminology provider, then the codes are used to query the data server.
      If the data server contains the necessary terminology resources and
      supports the token :in search modifier, setting this flag to false will
      enable code filtering directly on the data server which should improve
      CQL engine throughput.
      Default: false
    --filter
      Filter information for resource loader if the resource loader supports
      filtering
    -f, --format
      Output format of the report (JSON|TEXT*)
      Default: TEXT
      Possible Values: [TEXT, JSON]
    -h, --help
      Display this help
    -e, --include-evaluated-resources
      Include evaluated resources on measure report. To view resources must
      specify -f JSON.
      Default: false
    -j, --json-measure-configurations
      JSON File containing measure resource ids and optional parameters.
      Cannot be specified if -r option is used
    -m, --measure-server
      Path to configuration data for the FHIR knowledge assets. This will be
      either a JSON configuration file containing FHIR server connection
      details or the path to a file containing the FHIR resources of interest.
    -p, --parameters
      Parameter value(s) in format name:type:value where value can contain
      additional parameterized elements separated by comma. Multiple
      parameters must be specified as multiple -p options
    -r, --resource
      FHIR Resource ID or canonical URL for the measure resource to be
      evaluated. Cannot be specified if -j option is used
    --search-page-size
      Specifies how many records are requested per page during a FHIR search
      operation. The default value for servers can be quite small and setting
      this to a larger number will potentially improve performance.
      Default: 1000
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve terminology.
```

The quality measure evaluator CLI uses the `-r` command-line option to identify the clinical quality measure resource to evaluate. Measure resources can be loaded from a remote FHIR server, from a local filesystem folder, or from a ZIP archive stored in the local filesystem. For example, `-m /path/to/folder` or `-m /path/to/archive.zip` will load FHIR resources contained in those locations. If the option value is neither a folder or a ZIP archive, it is assumed to be the path to a JSON configuration file with connection details for the remote file server that contains the measure resource identified in the `-r` option.

If the user needs to evaluate more than one measure resource in a single program execution, the "-j" parameter can be used to provide a JSON file that describes all the measure evaluations to perform.

## Passing parameters on the command line (-p option)

The `-p` argument can be used to pass parameters only if the `-r` option is used to specify a single resource id
to evaluate. Multiple parameters must be provided by including the `-p` argument once per parameter. Each parameter
must follow the format `name:type:value`. If `type` is an interval, then `value` must be of the format `subtype,start,end`.

### Parameter Examples

#### Single Simple Parameter

```text
-p "param1:integer:10"
```

#### Single Interval Parameter

```text
-p "param2:interval:decimal,4.3,100.7"
```

#### Multiple Parameters

```text
-p "param3:decimal:50.9" -p "param55:interval:integer,30,40"
```

## Evidence Options

There are two options dedicated to expanding the measure report for better understanding of the evidence being provided, `includeEvaluatedResources` and `defineReturnOption`.

### includeEvaluatedResources
If specified, this option will populate the measure report's evaluatedResources property with the resources that were used in evaluating a patient. By default, this value is false.

### defineReturnOption
The define return option for evaluated define statements on a measure report. An evidence extension will be added to the measure report with the value of the appropriate defines. The options for the defines are ALL, NONE, or BOOLEAN. If a resource is added to the evidence, the value displayed will be a reference to a resource, not the resource itself.

```
{
  "resourceType": "MeasureReport",
    "extension": [
    {
       "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence",
       "extension": [
       {
         "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence-text",
         "valueString": "COL_InitialPop.Patient"
       },
       {
         "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence-value",
         "valueReference": {
           "reference": "Patient/123"
         }
       }
       ]
    },
     {
       "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence",
       "extension": [
       {
         "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence-text",
         "valueString": "COL_InitialPop.50-74years"
       },
       {
         "url": "http://ibm.com/fhir/cdm/StructureDefinition/measure-report-evidence-value",
         "valueBoolean": true
       }
       ]
     }
  ...
}
```

This option is intended for CQL debugging purposes, and will default to NONE.

## JSON File for -j Argument

The `-j` argument should be the path to a file containing a JSON object containing a list of measure configurations.
Configurations currently support measure ids and any parameters that should be used when executing a particular measure.

If this argument is specified, then the `-r` option cannot be specified as well. Likewise, if `-p` is specified alongside
this argument its contents will be ignored.

Outer Structure:
```text
{
   "measureConfigurations" : [
      {
         "parameters" : {
            "ParameterName1" : {PARAMETER_1},
            "ParameterName2" : {PARAMETER_2},
                 ...
            "ParameterNameN" : {PARAMETER_N}
         },
         "measureId" : "STRING_IDENTIFIER",
         "identifier": {
            "value": "STRING_VALUE",
            "system": "STRING_SYSTEM"
         }
         "version": "STRING_VERSION"
      }
   ]
}
```

* `measureConfigurations` (Required):
    * Description:  List containing configurations for which measures to run.
* `measureConfigurations.measureId` (Optional*):
    * Description: Measure id (resource id or canonical URL) of a measure to evaluate.
    * Type: String
    * *Note*: *Is required when `identifier` is not specified.
* `measureConfigurations.parameters` (Optional):
    * Description: An optional list of one or more `Parameter` objects to use during evaluation for the corresponding measure.
    * Type: `Parameter` (see section below)
* `measureConfigurations.identifier.value` (Optional*):
    * Description: Identifier value of the measure to evaluate.
    * Type: String
    * *Note*: *Is required when `measureId` is not specified.
* `measureConfigurations.identifier.system` (Optional):
    * Description: Identifier system value. Can only be defined if `identifier.value` is also specified
    * Type: String
* `measureConfigurations.version` (Optional - may be used when `identifier` is specified):
    * Description: String value representing a measure version. Identifies a particular version of a measure to retrieve
      when combined with `identifier`.
    * Type: String

### Parameter Structure and Supported Types
Each entry in `measureConfigurations.parameters` must specify a key-value pair. The key is a string containing
the name of the parameter. The value is a JSON `Parameter` object of the following format:
```text
{
  "type": "PARAMETER_TYPE_STRING",
  "parameter-field-1": "value-1",
      ...
  "parameter-field-N": "value-N"
}
```

Each parameter will specify its type, and then one or more other fields depending on the type of parameter.
Supported parameter types are `integer`, `decimal`, `string`, `boolean`, `datetime`, `date`, `time`, `quantity`,
`ratio`, `interval`, `code`, and `concept`.

See [here](user-guide/parameter-formats.md#parameter-formats) for details about how to format each parameter type.

### Measure Retrieval for Evaluation
Each entry in `measureConfigurations` may identify a measure to evaluate using either `measureId` or a combination of `identifier`
and `version`. A measure configuration entry may not include both a `measureId` and `identifier`.

If `measureId` is provided, the measure resource with the corresponding id in the configured FHIR server will be
retrieved and evaluated.

If `identifier` is provided without a version, then these steps will be followed:
  1. Retrieve any measures matching the provided `identifier.value` (and `identifier.system` if included).
  1. Discard any measures without a semantic version (numeric values matching the pattern x.y.z).
  1. Out of the remaining measures, return and evaluate the measure with the latest semantic version.

If `identifier` is provided with a version, then a measure with the provided `identifier.value` and `version` (along with
an optional `identifier.system` will be retrieved and evaluated).

Regardless of the provided configuration used to identify a measure, following any of the processes above must resolve
to a single measure in each case. If a measure cannot be retrieved using the provided criteria, or if two or more measures
match the provided criteria, then an error will be thrown.

### Examples of JSON Files
Here are some example JSON objects that could appear in a file for the `-j` argument.

#### Example 1: Single measure without parameters
```json
{
   "measureConfigurations" : [
      {
         "measureId" : "measure-with-id-1"
      }
   ]
}
```
The measure with resource id `measure-with-id-1` will be executed for each patient context without any parameters.

#### Example 2: Single measure with parameters
```json
{
   "measureConfigurations" : [
      {
         "measureId" : "measure-with-id-2",
         "parameters" : {
            "param1": {
               "type" : "integer",
               "value" : 20
            },
            "param2": {
               "type" : "interval",
               "start" : {
                  "type": "decimal",
                  "value": "4.0"
               },
               "startInclusive" : true,
               "end" : {
                  "type": "decimal",
                  "value": "7.5"
              },
              "endInclusive" : true
            }
         }
      }
   ]
}
```
The measure with resource id `measure-with-id-2` will be executed for each patient context with parameters `param1`
and `param2`.

#### Example 3: Multiple measures with and without parameters
```json
{
   "measureConfigurations" : [
      {
         "measureId" : "measure-with-id-3",
         "parameters" : {
            "param1": {
               "type" : "integer",
               "value" : "20"
            }
         }
      },
      {
         "measureId" : "measure-with-id-4"
      }
   ]
}
```
The measure with resource id `measure-with-id-3` will be executed for each patient context with the parameter `param1`.
The measure with resource id `measure-with-id-4` will be executed for each patient context without parameters.

#### Example 4: Multiple measures with identifiers
```json
{
   "measureConfigurations" : [
      {
         "identifier" : {
            "value" : "measure-with-identifier",
            "system" : "http://fakesystem.org"
         }
      },
      {
         "identifier" : {
            "value" : "measure-with-identifier"
         }
      },
      {
        "identifier" : {
          "value" : "measure-with-identifier",
          "system" : "http://fakesystem.org"
        },
        "version" : "1.2.3"
      }
   ]
}
```

The first entry will evaluate the measure with the latest semantic version where the measure's `identifier.value` is
equal to `measure-with-identifier` and `identifier.system` is equal to `http://fakesystem.org`.

The second entry will evaluate the measure with the latest semantic version where the measure's `identifier.value` is
equal to `measure-with-identifier` (regardless of `identifier.system` on each measure).

The third entry will evaluate the measure where `identifier.value` is equal to `measure-with-identifier`,
`identifier.system` is equal to `http://fakesystem.org`, and the measure version is exactly `1.2.3` (even if a later
version of a measure exists in the FHIR server with the same identifier fields).

### Error Checking for the -j Argument
These are the known error cases for the `-j` argument:

#### Missing argument
If the `-j` argument is not provided to the `MeasureCLI` and the `-r` argument is also missing then an
`IllegalArgumentException` will be thrown.

#### File does not exist
If the `-j` argument is provided, but the file does not exist, then a `IllegalArgumentException` will be thrown.

#### Unrecognized JSON fields
If the JSON object contains unexpected fields a `com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException` will
be thrown.

#### Missing fields in the JSON object
If the JSON object does not contain required fields, an `IllegalArgumentException` exception will be thrown. Cases that
can cause this:
* `measureConfigurations` is missing or is an empty list.
* Neither `measureId` nor `identifier.value` is specified for an entry in the `measureConfigurations` list.
* Both `measureId` and `identifier.value` are specified for a single entry in the `measureConfigurations` list.
* `identifier` is specified, but is missing `value` for an entry in the `measureConfigurations` list.
* One or more parameters is missing a required field.

#### Unsupported type and subtype values
If an unsupported `type` specified for a `Parameter`, then an `InvalidTypeIdException` will be thrown.

#### Incompatible values for a type
Various exceptions can be thrown when a `value` cannot be converted to the corresponding `type`.


## Running measure evaluation

With a local IBM FHIR test instance pre-loaded with a sample measure and patient...

```
$ java -Djavax.net.ssl.trustStore=config/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12 -classpath target/cohort-cli-VERSION-shaded.jar com.ibm.cohort.cli.MeasureCLI -d config/local-ibm-fhir.json -p path/to/json/parameter/file -c '1747edf2ef3-cd7133f1-3131-4ba8-a71a-da98c594cbab'
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 5.0.2 - Rev ecf175a352
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO ca.uhn.fhir.util.XmlUtil - Unable to determine StAX implementation: java.xml/META-INF/MANIFEST.MF not found
Evaluating: 1747edf2ef3-cd7133f1-3131-4ba8-a71a-da98c594cbab
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO org.opencds.cqf.r4.evaluation.MeasureEvaluation - Generating individual report
Population: initial-population = 1
Population: numerator = 0
Population: denominator = 1
---
```

Example contents of `path/to/json/parameter/file`:

```json
{
  "measureConfigurations": [
    {
      "measureId" : "wh-cohort-Over-the-Hill-Female-1.0.0"
    }
  ]
}
```
