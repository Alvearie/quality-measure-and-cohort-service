# User Guide - Getting Started

The [IBM Quality Measure and Cohort Engine](https://github.com/Alvearie/quality-measure-and-cohort-service/) is an open source project under the [Alvearie](https://github.com/Alvearie/) organization in Github. The Cohort Engine provides APIs for evaluating cohorts via definitions written in Clinical Quality Language (CQL). Users can execute CQL scripts directly using the CqlEngineWrapper API or indirectly through the use of FHIR measure and library resources. 

Since moving the project into Alvearie organization, publication of release builds has been temporarily put on hold until a suitable location for the builds has been determined. In the meantime, you will want to use "git clone" to pull down [the repository](https://github.com/Alvearie/quality-measure-and-cohort-service) and ``mvn install -f cohort-parent`` to build it. If you don't already have Maven installed on your workstation, [download](https://maven.apache.org/download.cgi) version 3.6.3 or newer and follow the [installation instructions](https://maven.apache.org/install.html). You should be using a Java SDK version 8.0 or higher. If you don't already have a Java SDK on your workstation, you can download one [here](https://adoptopenjdk.net/).

Once the prerequisites are installed and you have successfully executed the ``mvn install -f cohort-parent`` command, there will be a library JAR produced under cohort-engine/target and two JARs under cohort-cli/target. Under cohort-cli/target, one jar is the project without dependencies (cohort-cli-VERSION.jar) and the other is an "uber jar" (aka shaded jar) that contains all the project code and dependencies in a single executable artifact (cohort-cli-VERSION-shaded.jar). Choose the artifact that makes the most sense for your execution environment. Simple command-line testing is easiest with the shaded JAR.

The CQL engine utilizes data and terminology stored in a FHIR server adhering to the FHIR R4 standard. Our testing is focused on use of the [IBM Open Source FHIR Server](https://github.com/IBM/FHIR) server, but any FHIR server, such as the [open source HAPI FHIR server](https://github.com/jamesagnew/hapi-fhir), should be usable. The configuration and management of the FHIR server is left to the user. Configuration details for connecting to the server are provided to the API and can be managed in JSON files as needed (see below).

A common pattern is to have the FHIR server deployed in the IBM Cloud. For instance, you might grab the [latest Docker image](https://hub.docker.com/r/ibmcom/ibm-fhir-server) of the IBM FHIR Server and deploy it to a Kubernetes cluster in your IBM cloud account.  If your FHIR server is in IBM cloud and you don't have direct network line of sight to the server, you can use Kubernetes port-forward to point the engine the instance in the cloud using a command similar to the one below (substituting the <xxx> variables as appropriate for your environment).

`kubectl -n <kubernetes-namespace> port-forward service/<fhir-service> 9443:9443`

If you don't have a FHIR server of your own to use, you can test with [http://hapi.fhir.org/baseR4](http://hapi.fhir.org/baseR4). Sample configuration files are provided in the config directory that will connect to either a local or port-forwarded IBM FHIR server (config/local-ibm-fhir.json) or the public HAPI FHIR server (config/remote-hapi-fhir.json).  Three possible servers can be configured - a data server, a terminology server, and a measure resource server. Only the data server configuration is required. If the terminology server or measure resource server configuration is not provided, it is assumed that the data server connection will be used. The separation allows for more complex configurations where terminology or library resources are spread across multiple FHIR tenants (for example, using a shared tenant for terminology and measure resources).

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
$ java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -f src/test/resources/cql/basic -l test -c 1235008
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
$ java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -f src/test/resources/cql/parameters -l test-with -v params -c 1235008 -p MaxAge:integer:40
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
$ java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar --help
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

The Cohort Engine uses FHIR REST APIs to retrieve the information needed to process CQL queries. All FHIR servers are assumed to be R4. The ``-d`` argument to the CohortCLI is a file path pointing to a configuration file in JSON format that provides the details for connecting to the FHIR server that contains the resources that will be queried. There are two sample configuration files provided in the ``config`` folder. The ``config/remote-hapi-fhir.json`` example shows the most basic configuration which is simply the base URL of the target FHIR server. The ``config/local-ibm-fhir.json`` configuration shows a more advanced setup that uses basic authentication to connect to the FHIR server and provides a value for the IBM FHIR server tenant ID header which is used to specify which tenant in a multi-tenant environment will be used at runtime. These use the com.ibm.cohort.engine.FhirServerConfig and com.ibm.cohort.engine.IBMFhirServerConfig classes, respectively, and the @class property is required to indicate what type of configuration is being specified. Additional configuration is possible for developers that extend FhirServerConfig class and implement their own FhirClientBuilderFactory. A client builder factory can be configured using the com.ibm.cohort.FhirClientBuilderFactory.

In a configuration file using the IBMFhirServerConfig class, custom headers will be added to the FHIR REST API calls for the tenantId and datasource name as needed. The header names for these headers are set to the defaults used by the IBM FHIR server if not specified. Users can use the tenantIdHeader and dataSourceIdHeader properties to override the defaults if they've been changed in the target FHIR server's ``fhir-server-config.json`` file. Use ``dataSourceId`` as a property in the file to override the FHIR server datasource name (not shown in the example file). See the [IBM FHIR Server User's Guide](https://ibm.github.io/FHIR/guides/FHIRServerUsersGuide#33-tenant-specific-configuration-properties) for complete details on configuration, headers, and persistence setup.

Only one tenant's data can be queried per execution. There are, however, additional configuration options for the terminology server ``-t`` and measure server ``-m`` connections. Using these optional parameters, you can specify additional FHIR server connections for where terminology and clinical quality measure resources are stored.  If either of ``-m`` or ``-t`` is not provided, the data server connection details are used for the respective connection.

#### Example Configuration with All Options
```json
{
    "@class": "com.ibm.cohort.engine.FhirServerConfig",
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

A similar command-line interface is provided for evaluation of a quality measure definition that is stored in a FHIR Measure resource. The input parameters are very similar with the main difference being that instead of "-f", "-l", and "-v", and "-e" options to identify the library resource(s) and expression(s) that should be evaluated, a FHIR Measure resource is used and the "-r" command-line parameter provides the resource ID of that resource.

```
$ java -classpath target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.cli.MeasureCLI --help
Usage: measure-engine [options]
  Options:
  * -c, --context-id
      FHIR resource ID for one or more patients to evaluate.
  * -d, --data-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve data.
    -h, --help
      Display this help
    -m, --measure-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve measure and library resources.
    -p, --parameters
      Parameter value(s) in format name:type:value where value can contain
      additional parameterized elements separated by comma
  * -r, --resource
      FHIR Resource ID for the measure resource to be evaluated
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will
      be used to retrieve terminology.

```

With a local IBM FHIR test instance pre-loaded with a sample measure and patient...

```
$ java -Djavax.net.ssl.trustStore=config/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12 -classpath target/cohort-cli-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.cli.MeasureCLI -d config/local-ibm-fhir.json -r wh-cohort-Over-the-Hill-Female-1.0.0 -c '1747edf2ef3-cd7133f1-3131-4ba8-a71a-da98c594cbab'
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

