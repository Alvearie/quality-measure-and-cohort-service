# User Guide - Getting Started

The [IBM Quality Measure and Cohort Engine](https://github.com/Alvearie/quality-measure-and-cohort-service/) is an open source project under the [Alvearie](https://github.com/Alvearie/) organization in Github. The Cohort Engine provides APIs for evaluating cohorts via definitions written in Clinical Quality Language (CQL). Users can execute CQL scripts directly using the CqlEngineWrapper API or indirectly through the use of FHIR measure and library resources.

Builds are published in [Github packages](https://github.com/orgs/Alvearie/packages?repo_name=quality-measure-and-cohort-service) or you can pull the source code and build it yourself. If you are using the precompiled artifacts, you will most likely want to start with [cohort-cli](https://github.com/Alvearie/quality-measure-and-cohort-service/packages/506888) and choose the latest shaded jar (more on that below). If you are building yourself, use``git clone`` to pull down [the repository](https://github.com/Alvearie/quality-measure-and-cohort-service) and ``mvn install -f cohort-parent`` to build it. If you don't already have Maven installed on your workstation, [download](https://maven.apache.org/download.cgi) version 3.6.3 or newer and follow the [installation instructions](https://maven.apache.org/install.html). You should be using a Java SDK version 8.0 or higher. If you don't already have a Java SDK on your workstation, you can download one [here](https://adoptopenjdk.net/).

When building yourself using the ``mvn install -f cohort-parent`` command, there will be a library JAR produced under ``cohort-engine/target`` and two client JARs under ``cohort-cli/target``. The ``cohort-engine/target`` JAR is intended to be consumed by applications that are including the cohort-engine capabilities wrapped up in a larger application. The cohort-cli JARs are for clients that wish to use either of the provided command line interfaces. One of the ``cohort-cli/target`` jars is the command-line interface without dependencies (cohort-cli-VERSION.jar) and the other is an "uber jar" (aka shaded jar) that contains all the project code and dependencies in a single executable artifact (cohort-cli-VERSION-shaded.jar). Choose the artifact that makes the most sense for your execution environment. Simple command-line testing is easiest with the shaded JAR.

The CQL engine utilizes data and terminology stored in a FHIR server adhering to the FHIR R4 standard. Our testing is focused on use of the [IBM Open Source FHIR Server](https://github.com/IBM/FHIR), but any reasonably robust FHIR server, such as the [open source HAPI FHIR server](https://github.com/jamesagnew/hapi-fhir), should be usable. The configuration and management of the FHIR server is left to the user. Configuration details for connecting to the server are provided to the API and can be managed in JSON files as needed (see below).

A common pattern is to have the FHIR server deployed in the IBM Cloud. For instance, you might grab the [latest Docker image](https://hub.docker.com/r/ibmcom/ibm-fhir-server) of the IBM FHIR Server and deploy it to a Kubernetes cluster in your IBM cloud account.  If your FHIR server is in IBM cloud and you don't have direct network line of sight to the server, you can use Kubernetes port-forward to point the engine the instance in the cloud using a command similar to the one below (substituting the <xxx> variables as appropriate for your environment).

```
kubectl -n <kubernetes-namespace> port-forward service/<fhir-service> 9443:9443
```

If you don't have a FHIR server of your own to use, you can test with [http://hapi.fhir.org/baseR4](http://hapi.fhir.org/baseR4).

Sample configuration files are provided in the config directory that will connect to either a local or port-forwarded IBM FHIR server (config/local-ibm-fhir.json) or the public HAPI FHIR server (config/remote-hapi-fhir.json).  Three possible servers can be configured - a data server, a terminology server, and a measure resource server. Only the data server configuration is required. If the terminology server or measure resource server configuration is not provided, it is assumed that the data server connection will be used. The separation allows for more complex configurations where terminology or measure and library resources are spread across multiple FHIR tenants (for example, using a shared tenant for terminology and measure resources). More detail on the [FHIR Server Configuration](#fhir-server-configuration) can be found in later sections.

### Secure Socket Layer (SSL) Configuration

If you choose to use an IBM FHIR option, you will need to consider how to handle SSL setup. Out of the box, IBM FHIR only enables an SSL endpoint and not a plain text HTTP endpoint. You could enable the FHIR server plain text HTTP endpoint in the FHIR server's server.xml, but more than likely you will want to keep SSL in place and [configure your Java Virtual Machine (JVM) for SSL](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html). There is a sample java keystore config/trustStore.pkcs12 that contains the IBM Cloud ingress service SSL certificate. You can use the provided trustStore.pkcs12 (or your own as needed) by passing standard arguments to your JVM on startup. An example that uses the provided trustStore file would include `-Djavax.net.ssl.trustStore=config/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12`.

## CQL Definitions

CQL is a public specification and there are a number of helpful resources on the web for learning about the language (see below) or you can use the sample CQL definitions under src/test/resources/cql if you don't have your own.

* Language Specification - [http://cql.hl7.org](http://cql.hl7.org)
* eCQI Resource Center CQL Language Education - [https://ecqi.healthit.gov/cql?qt-tabs_cql=2](https://ecqi.healthit.gov/cql?qt-tabs_cql=2)
* CQL Online Library Editor - [http://cql-online.esacinc.com/](http://cql-online.esacinc.com/)
* Electronic Clinical Quality Measure (eCQM) Clinical Quality Language (CQL) Basics Webinar -
[https://www.youtube.com/watch?v=3M8qeQNzXX8](https://www.youtube.com/watch?v=3M8qeQNzXX8)

## Command-line interface

A command-line interface (CLI) is provided for use in testing the cohort engine. There is a simple CLI execution packaged with the Maven build that can be executed from the cohort-cli project using ``mvn exec:java``. This will demonstrate connectivity with the public HAPI FHIR server using a very simple CQL library. An execution where you run the command yourself would look like the following...

```bash
$ java -jar target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -f src/test/resources/cql/basic -l test -c 1235008
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 5.0.2 - Rev ecf175a352
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO ca.uhn.fhir.util.XmlUtil - Unable to determine StAX implementation: java.xml/META-INF/MANIFEST.MF not found
Loading libraries from folder 'src\test\resources\cql\basic'
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
Context: 1235008
Expression: Patient, Result: org.hl7.fhir.r4.model.Patient@3e4e8fdf
Expression: Female, Result: false
Expression: Male, Result: true
Expression: Over the hill, Result: true
---
```

Or if you need to pass parameters...

```bash
$ java -jar target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -f src/test/resources/cql/parameters -l test-with -v params -c 1235008 -p MaxAge:integer:40
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 5.0.2 - Rev ecf175a352
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO ca.uhn.fhir.util.XmlUtil - Unable to determine StAX implementation: java.xml/META-INF/MANIFEST.MF not found
Loading libraries from folder 'src\test\resources\cql\parameters'
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
Context: 1235008
Expression: Patient, Result: org.hl7.fhir.r4.model.Patient@72503b19
Expression: Female, Result: false
Expression: Male, Result: true
Expression: ParamMaxAge, Result: 40
---
```

Complete usage is available using the --help flag...

```
$ java -jar target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar --help
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 5.0.2 - Rev ecf175a352
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
Usage: cql-engine [options]
  Options:
  * -c, --context-id
      Unique ID for one or more context objects (e.g. Patient IDs)
  * -d, --data-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve data.
    -e, --expression
      ELM Expression(s) to Execute
  * -f, --files
      Resource that contains the CQL library sources. Valid options are the
      path to a zip file or folder containing the cohort definitions or the
      resource ID of a FHIR Library resource contained in the measure server.
    -h, --help
      Display this help
  * -l, --libraryName
      Library Name (from CQL Library statement)
    -v, --libraryVersion
      Library Version (from CQL Library statement)
    -m, --measure-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve measure and library resources.
    -p, --parameters
      Parameter value(s) in format name:type:value where value can contain
      additional parameterized elements separated by comma
    -s, --source-format
      Indicates which files in the file source should be processed
      Default: XML
      Possible Values: [CQL, XML]
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve terminology.
```

### FHIR Server Configuration

The Cohort Engine uses FHIR REST APIs to retrieve the information needed to process CQL queries. All FHIR servers are assumed to be R4. The ``-d`` argument to the CohortCLI is a file path pointing to a configuration file in JSON format that provides the details for connecting to the FHIR server that contains the resources that will be queried. There are two sample configuration files provided in the ``config`` folder. The ``config/remote-hapi-fhir.json`` example shows the most basic configuration which is simply the base URL of the target FHIR server. The ``config/local-ibm-fhir.json`` configuration shows a more advanced setup that uses basic authentication to connect to the FHIR server and provides a value for the IBM FHIR server tenant ID header which is used to specify which tenant in a multi-tenant environment will be used at runtime. These use the com.ibm.cohort.fhir.client.config.FhirServerConfig and com.ibm.cohort.fhir.client.config.IBMFhirServerConfig classes, respectively, and the @class property is required to indicate what type of configuration is being specified. Additional configuration is possible for developers that extend FhirServerConfig class and implement their own FhirClientBuilderFactory. A client builder factory can be configured using the com.ibm.cohort.FhirClientBuilderFactory.

In a configuration file using the IBMFhirServerConfig class, custom headers will be added to the FHIR REST API calls for the tenantId and datasource name as needed. The header names for these headers are set to the defaults used by the IBM FHIR server if not specified. Users can use the tenantIdHeader and dataSourceIdHeader properties to override the defaults if they've been changed in the target FHIR server's ``fhir-server-config.json`` file. Use ``dataSourceId`` as a property in the file to override the FHIR server datasource name (not shown in the example file). See the [IBM FHIR Server User's Guide](https://ibm.github.io/FHIR/guides/FHIRServerUsersGuide#33-tenant-specific-configuration-properties) for complete details on configuration, headers, and persistence setup.

Only one tenant's data can be queried per execution. There are, however, additional configuration options for the terminology server ``-t`` and measure server ``-m`` connections. Using these optional parameters, you can specify additional FHIR server connections for where terminology and clinical quality measure resources are stored.  If either of ``-m`` or ``-t`` is not provided, the data server connection details are used for the respective connection.

#### Example Configuration with All Options
```json
{
    "@class": "com.ibm.cohort.fhir.client.config.FhirServerConfig",
    "endpoint": "https://localhost:9443/fhir-server/api/v4",
    "user": "fhiruser",
    "password": "change-password",
	"token": "token",
	"cookies": [ "ocookie=mycookie" ],
	"headers": {
		"X-FHIR-TENANT-ID": "default",
		"X-FHIR-DS-ID": "default"
	},
	"logInfo": [
		"ALL"
	]
}
```

Users should choose zero or one authentication scheme. If both user/password credentials and bearer authorization token are provided the behavior is non-prescriptive. Valid values for the logInfo configuration are ALL, REQUEST_SUMMARY, REQUEST_BODY, REQUEST_HEADERS, RESPONSE_SUMMARY, RESPONSE_HEADERS, RESPONSE_BODY. See the [HAPI FHIR Client Configuration guide](https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section1) for full details. You must have your logger configured at INFO level or above to see the log messages generated by the logInfo settings. See the [Logging](#logging) section for additional details.

### Input Parameters

Parameters on the command-line are represented as colon-delimited triplet data in the format ``name:type:value``. Names should exact case match the definitions in the CQL library being used. The type is a lowercase value that matches the CQL datatype of the parameter. Value literals for date types should be prefixed with the @ sign per the CQL specification (e.g. ``MyParam:date:@1970-01-01``) and time types should be prefixed with the letter T (e.g. MyParam:time:T12:00:00). For data types that have structured data associated with them, such as an interval or quantity parameter, the value field contains additional formatting. For quantity parameters, the amount and unit of measure are colon-delimited (e.g. MyParam:quantity:10,mg/mL). For interval parameters, the value parameter is comma-delimited (to avoid collisions with date-time strings) and is triplet of ``datatype,start,end`` where the start and end parameters follow the standard rules for those types as above.

### Library loaders

Several library loading mechanisms are supplied with the engine. The location of the library code is provided using the ``-f <libraryResource>`` command-line parameter. The engine will check if the resource is a folder, ZIP file, or if the resource is neither a folder or ZIP file then it is assumed to be the resource ID of a FHIR Library resource that contains the CQL definitions.

For the folder and ZIP library loading mechanisms, filenames are assumed to be in the format ``name-version`` with either a .cql or .xml file extension. Other possible ELM formats, such as JSON, are not currently supported. Files that do not match supported criteria are ignored. Multi-level directory structure can be used if desired.

For the FHIR Library resource loader, CQL is embedded in the ``content`` field as FHIR Attachment resources. Per recommendations in the Data Exchange for Quality Measures (DEQM) FHIR Implementation guide, FHIR Attachment resources are assumed to be Base64 encoded and ``contentType`` field should be set to text/cql for CQL attachments and application/elm+xml for ELM attachments. The Library resource ID that is provided is considered to be the "main" artifact and should contain the expressions that the user would like to evaluate. Additional libraries can be included through the use of the ``relatedArtifacts`` field in the library resource. And relatedArtifacts entries that are Library resources will also be loaded and loading is done recursively.

## Logging
Logging in Java can be a complicated topic because there are a variety of logging frameworks available and combining open source projects together often ends up with multiple different strategies in use at the same time. SLF4J is the preferred logging framework for cohort engine use. Users should refer to the [SLF4j manual](http://www.slf4j.org/manual.html) for instructions on how to configure SLF4J for their purposes. When executing from the command-line, the slf4j-simple binding is used. Configuration can be provided via Java system properties. See [the SimpleLogger documentation](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html) for complete details.

The APIs leverage the Hibernate reference implementation for bean validation which uses JBoss logging. If users wish to control the Hibernate logging via SLF4J then they should add ``-Dorg.jboss.logging.provider=slf4j`` to their system properties.

### Dealing with potential PHI leakage in the logs
<div style="background-color: beige; color: red; border: 1px solid red;">The Execution Logical Model (ELM) specification has a <a href="https://cql.hl7.org/04-logicalspecification.html#errors-and-messages">Message</a> feature that can be leveraged by CQL authors to write debug messages to the logging framework. The contents of the debug message can include any amount of detail in the objects being processed by the engine. In a situation where PHI is being processed, care should be taken to avoid logging PHI using the "Message" feature or to disable the logging entirely in the solution's logging framework configuration. The logging comes from <span style="font-family: Courier New; color: black;">org.opencds.cqf.cql.engine.elm.execution.MessageEvaluator</span>. This could be disabled from the SimpleLogger implementation used during CLI execution by setting a system property as shown below.</div>

```
java -jar cohort-cli-*.jar -Dorg.slf4j.simpleLogger.log.org.opencds.cqf.cql.engine.elm.execution.MessageEvaluator=off ...
```


# Error states
The Engine detects and throws IllegalArgumentException for the following error states:
1) When the CqlEngineWrapper.evaluate(...) method is invoked without first configuring the library sources, data server, and terminology server settings.
2) When the CqlEngineWrapper.evaluate(...) method is invoked without providing a library name and at least one context ID (aka patient ID).
3) When the CqlEngineWrapper.evaluate(...) method is invoked for a CQL Library that contains parameters with no default value and no value is provided in the method's parameters map.
4) When the CqlEngineWrapper.evaluate(...) method is invoked for a CQL Library that is not loaded
5) When the CqlEngineWrapper.main(...) method is invoked with an invalid parameter type or interval subtype

Connectivity issues to the FHIR server are reported through the HAPI FHIR Client library. See HAPI documentation for complete details, but an example exception would be ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException.

An UnsupportedOperationException is thrown if a parameter type of "concept" is attempted from the command line. Concept parameters are complex and unlikely to be necessary, so no effort was made to support them right now.

When a context (aka Patient ID) is provided that does not match a valid Patient resource in the FHIR server, HAPI will bubble up and HTTP 404 exception. And HTTP 404 might also be thrown if FHIR Library resource loading is used and the provided Library ID does not exist.

# Measure Evaluation

A similar command-line interface is provided for evaluation of a quality measure definition that is stored in a FHIR
Measure resource. The input parameters are very similar with the main difference being that instead of
"-f", "-l", and "-v", and "-e" options to identify the library resource(s) and expression(s) that should be evaluated,
a FHIR Measure resource is used and the "-r" command-line parameter provides the resource ID of that resource.
Alternatively, the "-j" paramter can be used to provide a JSON file containing measure resource configurations to use
during measure evaluation.

```
$ java -classpath cohort-cli/target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.cli.MeasureCLI --help
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
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will 
      be used to retrieve terminology.
```

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
$ java -Djavax.net.ssl.trustStore=config/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12 -classpath target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.cli.MeasureCLI -d config/local-ibm-fhir.json -p path/to/json/parameter/file -c '1747edf2ef3-cd7133f1-3131-4ba8-a71a-da98c594cbab'
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

## FHIR Resource Import

There is a tooling project provided for loading necessary resources into a target FHIR server. The project is fhir-resource-tooling and it produces a shaded JAR ```fhir-resource-tooling-0.0.1-SNAPSHOT-shaded.jar``` that provides a simple command line interface for loading the resources. The JAR provides two tools, one for loading resources exported from the Measure Authoring Tool (zip files), and one for loading VSAC Value Set spreadsheets as downloaded from https://cts.nlm.nih.gov/fhir.

Configuration of the importer tooling uses the same JSON configuration file as the cohort-engine and measure-evaluator CLI tools described above. Users should also make sure to provide whatever SSL configuration settings are needed as described in [Secure Socket Layer (SSL) Configuration](#secure-socket-layer-ssl-configuration) section above.

### Measure ZIP
To load Measure and Library resources exported from the Measure Authoring Tool (zip files) into a target FHIR server, use the following invocation:

```
$> java -jar fhir-resource-tooling-0.0.1-SNAPSHOT-shaded.jar -m local-fhir-config.json /path/to/measurebundle.zip
```

Requests are processed as FHIR bundles and the contents of the request and response bundles are written to an output path specified by the ``-o`` option. If no ``-o`` option is provided then the output is written to the current working directory. Users are responsible for creating the directory used in the -o option prior to executing the importer. The directory will not be created for you.

### VSAC Spreadsheet
In order to use this tool, you will need to download spreadsheets from the VSAC website. Start at the VSAC authority website [here](https://vsac.nlm.nih.gov/). Navigate to the Search Value Sets tab to look for specific value sets, and select Export Value Set Results on relevent sets.
<br>To load value set resources downloaded from the NIH website (spreadsheets, not zip files) into a target FHIR server, use the following invocation:

```
$> java -cp fhir-resource-tooling-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.tooling.fhir.VSACValueSetImporter -m local-fhir-config.json <list_of_spreadsheet_valuesets>
```

To see an existing template one can use to build one's own value sets from scratch, please check out our [template](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/docs/user-guide/value_set_template.xlsx).
