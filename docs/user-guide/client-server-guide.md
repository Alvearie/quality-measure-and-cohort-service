
# Client-Server Mode

In client-server mode, evaluation of clinical quality language (CQL) queries is done in a process that retrieves data from a remote FHIR server that supports version R4 of the HL7 FHIR RESTful API. Older releases of FHIR are not supported at this time. This mode of operation provides the most flexibility for deployments while sacrificing some runtime performance for that flexibility. Users needing additional performance should consider the [server-only](user-guide/server-only-guide.md) and [big-data](user-guide/spark-user-guide.md) modes of operation as alternatives to client-server mode.

## Picking a FHIR Server

In order to use client-server mode, you must have an R4 compliant FHIR server available to interact with. Internal project testing is done using the [IBM Open Source FHIR Server](https://github.com/IBM/FHIR). The easiest way to get started is to install something like Docker Desktop or minikube on your local system and then grab the [latest Docker image](https://hub.docker.com/r/ibmcom/ibm-fhir-server) of the IBM FHIR Server and run it. Alternatively, you might deploy the image to the IBM Kubernetes Service in your IBM cloud account or clone the IBM FHIR Server and run it in a local [Open Liberty](https://openliberty.io) server. Whatever you choose, you will need a running FHIR server to test the cohort evaluation capabilities. If you want to skip this step entirely, you can consider testing with a public FHIR server such as [http://hapi.fhir.org/baseR4](http://hapi.fhir.org/baseR4).

The details for how to configure an IBM FHIR server are specific to the application you are building. However, the cohort-and-quality-measure-service team publishes some recommended configuration settings under [FHIR Server Installation & Configuration](user-guide/fhir-server-config.md) that may be helpful when planning your deployment.

If your FHIR server is running under Kubernetes and you don't have direct network line of sight to the server, you can use Kubernetes port-forward to point the engine the instance in the cloud using a command similar to the one below (substituting the \<xxx\> variables as appropriate for your environment).

```
kubectl -n <kubernetes-namespace> port-forward service/<fhir-service> 9443:9443
```

## Choose an interaction model

There are lots of ways to do CQL evaluation in client-server mode. The simplest place to get started is to use the [command-line interface](#command-line-interface). If your application needs a standard-agnostic REST API to work with, you might choose to deploy the cohort-services API project (cohort-engine-api-web) or the provided Docker image. Alternatively, you might choose to integrate the cohort-engine APIs directly into your application. The choice is up to you.

### Command-line interface

The cohort evaluator command-line interface (CLI) provides a simple interface for users to evaluate clinical quality language queries stored in filesystem files, ZIP archives, or FHIR Library resources against data in a remote FHIR server. A simple CLI execution is descried in the cohort-cli package's Maven build artifact and can be executed from using `mvn exec:java` command. That will demonstrate program usage using the public https://hapi.fhir.org/baseR4 FHIR server and a very simple CQL library. An execution where you run the command yourself would look like the following...

```bash
$ java -jar target/cohort-cli-VERSION-shaded.jar cohort-cli -d config/remote-hapi-fhir.json -f src/test/resources/cql/basic -l test -c 1235008
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
$ java -jar target/cohort-cli-VERSION-shaded.jar cohort-cli -d config/remote-hapi-fhir.json -f src/test/resources/cql/parameters -l test-with -v params -c 1235008 -p MaxAge:integer:40
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

Complete usage of the CLI is available using the --help flag...

```
$ java -jar target/cohort-cli-VERSION-shaded.jar cohort-cli --help
Usage: cql-engine [options]
  Options:
  * -c, --context-id
      Unique ID for one or more context objects (e.g. Patient IDs)
  * -d, --data-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve data.
    --enable-terminology-optimization
      By default, ValueSet resources used in CQL are first expanded by the
      terminology provider, then the codes are used to query the data server.
      If the data server contains the necessary terminology resources and
      supports the token :in search modifier, setting this flag to false will
      enable code filtering directly on the data server which should improve
      CQL engine throughput.
      Default: false
    -e, --expression
      ELM Expression(s) to Execute
  * -f, --files
      Resource that contains the CQL library sources. Valid options are the
      path to a zip file or folder containing the cohort definitions or the
      resource ID of a FHIR Library resource contained in the measure server.
    --filter
      Additional filters to apply to library loaders if supported by the
      library loading mechansim
    -h, --help
      Display this help
  * -l, --libraryName
      Library Name
    -v, --libraryVersion
      Library Version
    --logging-level
      Specific logging level
      Default: NA
      Possible Values: [COVERAGE, TRACE, NA]
    -m, --measure-server
      Path to configuration data for the FHIR knowledge assets. This will be
      either a JSON configuration file containing FHIR server connection
      details or the path to a file containing the FHIR resources of interest.
    -i, --model-info
      Model info file used when translating CQL
    -p, --parameters
      Parameter value(s) in format name:type:value where value can contain
      additional parameterized elements separated by comma. Multiple
      parameters must be specified as multiple -p options
    --search-page-size
      Specifies how many records are requested per page during a FHIR search
      operation. The default value for servers can be quite small and setting
      this to a larger number will potentially improve performance.
      Default: 1000
    -s, --source-format
      Indicates which files in the file source should be processed
      Default: XML
      Possible Values: [CQL, XML]
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve terminology.
```

### FHIR Server Configuration

The data server (`-d`), terminology server (`-t`), and measure server (`-m`) options of the cohort evaluator CLI are paths to configuration files that describe the various FHIR server connections that will be used during CQL evaluation. Only the `-d` option is required. When not provided, the `-t` and `-m` connections will default to the same connection details provided in the `-d` option.

There are two sample configuration files provided in the [cohort-cli/config](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/cohort-cli/config) folder. The [cohort-cli/config/remote-hapi-fhir.json](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-cli/config/remote-hapi-fhir.json) example shows the most basic configuration which is simply the base URL of the target FHIR server. The [cohort-cli/config/local-ibm-fhir.json](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-cli/config/local-ibm-fhir.json) configuration shows a more advanced setup that uses basic authentication to connect to the FHIR server and provides a value for the IBM FHIR server tenant ID header which is used to specify which tenant in a multi-tenant environment will be used at runtime. These use the [FhirServerConfig](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/fhir-client-config/src/main/java/com/ibm/cohort/fhir/client/config/FhirServerConfig.java) and [IBMFhirServerConfig](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/fhir-client-config/src/main/java/com/ibm/cohort/fhir/client/config/IBMFhirServerConfig.java) classes, respectively, and the `@class` property is required to indicate what type of configuration is being specified. Additional configuration is possible for developers that extend [FhirServerConfig](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/fhir-client-config/src/main/java/com/ibm/cohort/fhir/client/config/FhirServerConfig.java) class and implement their own [FhirClientBuilderFactory](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/fhir-client-config/src/main/java/com/ibm/cohort/fhir/client/config/FhirClientBuilderFactory.java). A client builder factory can be configured using the [FhirClientBuilderFactory](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/fhir-client-config/src/main/java/com/ibm/cohort/fhir/client/config/FhirClientBuilderFactory.java) class.

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
	],
	"socketTimeout": 10000,
	"connectTimeout": 10000,
	"connectionRequestTimeout": 10000
}
```

Users should choose zero or one authentication scheme. If both user/password credentials and bearer authorization token are provided the behavior is non-prescriptive. Valid values for the logInfo configuration are ALL, REQUEST_SUMMARY, REQUEST_BODY, REQUEST_HEADERS, RESPONSE_SUMMARY, RESPONSE_HEADERS, RESPONSE_BODY. See the [HAPI FHIR Client Configuration guide](https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#section1) for full details. You must have your logger configured at INFO level or above to see the log messages generated by the logInfo settings. See the [Logging](#logging) section for additional details.

The available timeout configuration options are as follows:
* `socketTimeout`: The amount of time in milliseconds that a read or write network operation may block without failing.
* `connectTimeout`: The amount of time in milliseconds that an initial connection attempt network operation may block without failing.
* `connectionRequestTimeout`: The amount of time in milliseconds to wait for an available HTTP connection from the connection
pool before failing.

If provided, each timeout value must be an integer >= 0. A value of 0 will be treated as an unlimited timeout. A default
of 10 seconds is used for each value not specified in the configuration file.

### Logging
Logging in Java can be a complicated topic because there are a variety of logging frameworks available and combining open source projects together often ends up with multiple different strategies in use at the same time. SLF4J is the preferred logging framework for cohort engine use. Users should refer to the [SLF4j manual](http://www.slf4j.org/manual.html) for instructions on how to configure SLF4J for their purposes. When executing from the command-line, the slf4j-simple binding is used. Configuration can be provided via Java system properties. See [the SimpleLogger documentation](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html) for complete details.

The APIs leverage the Hibernate reference implementation for bean validation which uses JBoss logging. If users wish to control the Hibernate logging via SLF4J then they should add `-Dorg.jboss.logging.provider=slf4j` to their system properties.

### Secure Socket Layer (SSL) Configuration

If you choose to use an IBM FHIR option, you will need to consider how to handle SSL setup. Out of the box, IBM FHIR only enables an SSL endpoint and not a plain text HTTP endpoint. You could enable the FHIR server plain text HTTP endpoint in the FHIR server's server.xml, but more than likely you will want to keep SSL in place and [configure your Java Virtual Machine (JVM) for SSL](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html). There is a sample java keystore [config/trustStore.pkcs12](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-cli/config/trustStore.pkcs12) that contains the default `localhost` certificate from the IBM FHIR server. You can use the provided trustStore.pkcs12 (or your own as needed) by passing standard arguments to your JVM on startup. An example that uses the provided trustStore file would include `-Djavax.net.ssl.trustStore=config/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12`.

### Command-line Input Parameters

Parameters on the command-line are represented as colon-delimited triplet data in the format `name:type:value`. Names should exact case match the definitions in the CQL library being used. The type is a lowercase value that matches the CQL datatype of the parameter. Value literals for date types should be prefixed with the @ sign per the CQL specification (e.g. `MyParam:date:@1970-01-01`) and time types should be prefixed with the letter T (e.g. `MyParam:time:T12:00:00`). For data types that have structured data associated with them, such as an interval or quantity parameter, the value field contains additional formatting. For quantity parameters, the amount and unit of measure are colon-delimited (e.g. `MyParam:quantity:10,mg/mL`). For interval parameters, the value parameter is comma-delimited (to avoid collisions with date-time strings) and is triplet of `datatype,start,end` where the start and end parameters follow the standard rules for those types as above.

### Library loaders

Several library loading mechanisms are supplied with the cohort evaluator component. The location of the library code is provided to the CLI using the `-f <libraryResource>` command-line parameter. The cohort evaluator will check if the resource is a folder, ZIP file, or if the resource is neither a folder or ZIP file then it is assumed to be the resource ID of a FHIR Library resource that contains the CQL definitions.

For the folder and ZIP library loading mechanisms, filenames are assumed to be in the format `name-version` with either a .cql or .xml file extension. Other possible ELM formats, such as JSON, are not currently supported. Files that do not match supported criteria are ignored. Multi-level directory structure can be used if desired.

For the FHIR Library resource loader, CQL is embedded in the `content` field as FHIR Attachment resources. Per recommendations in the Data Exchange for Quality Measures (DEQM) FHIR Implementation guide, FHIR Attachment resources are assumed to be Base64 encoded and `contentType` field should be set to text/cql for CQL attachments and application/elm+xml for ELM attachments. The Library resource ID that is provided is considered to be the "main" artifact and should contain the expressions that the user would like to evaluate. Additional libraries can be included through the use of the `relatedArtifacts` field in the library resource. And relatedArtifacts entries that are Library resources will also be loaded and loading is done recursively.

## Engine Execution Timezone
Engine executions currently run with a timezone of UTC. This means that if a CQL attempts to create a DateTime object
without timezone information provided, then it will default the DateTime's timezone to UTC. When authoring CQL or
creating parameters, we recommend always specifying a full DateTime, including the proper timezone, if possible.

CQL examples:
```text
  // No timezone information
  ToDateTime(Date(2020, 4, 4))         -->  2020-04-04T00:00:00.0Z
  DateTime(2020, 5, 5, 1, 2, 3, 4)     -->  2020-05-05T01:02:03.4Z

  // With timezone information
  DateTime(2020, 6, 6, 9, 8, 7, 6, -4) -->  2020-06-06T09:08:07.6-04:00
```

Parameter Examples:
```text
  // No timezone information
  "2020-01-01T12:30:00.0"       -->  2020-01-01T12:30:00.0Z

  // With timezone information
  "2020-07-07T04:15:00.0-04:00" -->  2020-07-07T04:15:00.0-04:0
```

# Error states
The Engine detects and throws IllegalArgumentException for the following error states:
1) When the CqlEvaluator.evaluate(...) method is invoked without first configuring the library sources, data server, and terminology server settings.
2) When the CqlEvaluator.evaluate(...) method is invoked without providing a library name and at least one context ID (aka patient ID).
3) When the CqlEvaluator.evaluate(...) method is invoked for a CQL Library that contains parameters with no default value and no value is provided in the method's parameters map.
4) When the CqlEvaluator.evaluate(...) method is invoked for a CQL Library that is not loaded
5) When the CqlEvaluator.main(...) method is invoked with an invalid parameter type or interval subtype

Connectivity issues to the FHIR server are reported through the HAPI FHIR Client library. See HAPI documentation for complete details, but an example exception would be ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException.

An UnsupportedOperationException is thrown if a parameter type of "concept" is attempted from the command line. Concept parameters are complex and unlikely to be necessary, so no effort was made to support them right now.

When a context (aka Patient ID) is provided that does not match a valid Patient resource in the FHIR server, HAPI will bubble up and HTTP 404 exception. And HTTP 404 might also be thrown if FHIR Library resource loading is used and the provided Library ID does not exist.
