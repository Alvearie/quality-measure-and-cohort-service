# User Guide - Getting Started

The IBM CQL Engine code is stored under https://github.ibm.com/watson-health-cohorting/cohort-engine. A basic wrapper around the open source CQL engine is provided in the CqlEngineWrapper class. Builds are defined using Maven assuming version 3.6.3 (latest). You can download Maven here - https://maven.apache.org/download.cgi.

Kwas is working on getting the builds automated and JARs pushed to Artifactory. In the meantime, you will want to use "git clone" to pull down the repository and "mvn install" to build it. As part of the default build there is an "uber jar" (aka shaded jar) produced that can be used as a simple entry point to the main method (e.g. java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar ...). 

You will need a FHIR server to interact with. If you have CDT access, you can use Kubernetes port-forward to point the engine at our instance in the cloud using a command similar to the one below.

`kubectl -n cdt-commops-quickteam01-ns-01 port-forward service/fhir-internal 9443:9443`

If you don't have CDT access or would prefer to use a server with more data, you can try http://hapi.fhir.org/baseR4. Either way FHIR configuration is provided as a JSON file. See config/local-ibm-fhir.json for an example that assumes you are using the IBM FHIR server in our CDT environment port-forwarded on your local box. If you go this route, make sure you pass `-Djavax.net.ssl.trustStore=config/trustStore.jks -Djavax.net.ssl.trustStorePassword=change-password` as JVM arguments or you will get SSL errors.

There are sample CQL definitions under src/test/resources/cql if you don't have your own. 

A sample execution would look something like this..

```bash
$ java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -t config/remote-hapi-fhir.json -f src/test/resources/cql/basic -l Test -c 1235008
Loading libraries from src\test\resources\cql\basic ...
Loaded file src\test\resources\cql\basic\test.xml
Loaded 1 libraries
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 4.2.0 - Rev 8491707942
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO ca.uhn.fhir.util.XmlUtil - Unable to determine StAX implementation: java.xml/META-INF/MANIFEST.MF not found
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
Expression: Patient, Context: 1235008, Result: Patient/org.hl7.fhir.r4.model.Patient@2b7e8044
Expression: Female, Context: 1235008, Result: Boolean/false
Expression: Male, Context: 1235008, Result: Boolean/true
Expression: Over the hill, Context: 1235008, Result: Boolean/true
```

Or if you need to pass parameters...

```bash
$ java -jar target/cohort-engine-0.0.1-SNAPSHOT-shaded.jar -d config/remote-hapi-fhir.json -t config/remote-hapi-fhir.json -f src/test/resources/cql/parameters -l Test -c 1235008 -p MaxAge:integer:40
Loading libraries from src\test\resources\cql\parameters ...
Loaded file src\test\resources\cql\parameters\test-with-params.xml
Loaded 1 libraries
[main] INFO ca.uhn.fhir.util.VersionUtil - HAPI FHIR version 4.2.0 - Rev 8491707942
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
[main] INFO ca.uhn.fhir.util.XmlUtil - Unable to determine StAX implementation: java.xml/META-INF/MANIFEST.MF not found
[main] INFO ca.uhn.fhir.context.FhirContext - Creating new FHIR context for FHIR version [R4]
Expression: Patient, Context: 1235008, Result: Patient/org.hl7.fhir.r4.model.Patient@6bc24e72
Expression: Female, Context: 1235008, Result: Boolean/false
Expression: Male, Context: 1235008, Result: Boolean/true
```

All CQL data types are supported as parameters types with lowercase type names. Intervals of integer, decimal, quantity, datetime, and time are supported. Date literals should start with an "@" sign (e.g. @2020-09-27). If no Timezone is provided in a datetime or time parameter value, the system default timezone is used.

# Error states
The Engine detects and throws IllegalArgumentException for the following error states:
1) When the CqlEngineWrapper.evaluate(...) method is invoked without first configuring the library sources, data server, and terminology server settings.
2) When the CqlEngineWrapper.evaluate(...) method is invoked without providing a library name and at least one context ID (aka patient ID).
3) When the CqlEngineWrapper.evaluate(...) method is invoked for a CQL Library that contains parameters with no default value and no value is provided in the method's parameters map.
4) When the CqlEngineWrapper.evaluate(...) method is invoked for a CQL Library that is not loaded
5) When the CqlEngineWrapper.main(...) method is invoked with an invalid parameter type or interval subtype

Connectivity issues to the FHIR server are reported through the HAPI FHIR Client library. See HAPI documentation for complete details, but an example exception would be ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException.

An UnsupportedOperationException is thrown if a parameter type of "concept" is attempted from the command line. Concept parameters are complex and unlikely to be necessary, so no effort was made to support them right now.

