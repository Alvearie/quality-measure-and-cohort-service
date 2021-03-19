Cohort Engine Flink Example
===========================
This project is intended to be a simple demonstration on how to leverage `MeasureEvaluator` within [Apache Flink](https://flink.apache.org/).

The provided drivers utilize [Apache Kafka](https://kafka.apache.org/) for input and output.

All connections to Kafka are done via plain login over `SASL_SSL`.
This aligns with the default configuration of [IBM Event Streams](https://www.ibm.com/cloud/event-streams).

Input Producer
--------------
Class: `com.ibm.cohort.engine.flink.input.InputProducer`

The Input Producer does exactly what's written on the tin.
It produces specified number of random input records to a specified Kafka topic given a set of measure and patient IDs.
Each input record is a random patient paired with every measure from the provided file.

The RNG is seeded to ensure consistent data.

##### Arguments
* `--kafkaBrokers <ARG>`: The list of Kafka brokers to connect to.
* `--kafkaPassword <ARG>`: The password to connect to Kafka.
* `--kafkaTopic <ARG>`: The Kafka topic to send input to.
* `--numRecords <ARG>`: The total number of records to produce.
* `--measureFile <ARG>`: A file containing a single FHIR measure ID per line.
* `--patientFile <ARG>`: A file containing a single FHIR patient ID per line.

Cohort Engine Flink Driver
--------------------------
Class: `com.ibm.cohort.engine.flink.execution.CohortEngineFlinkDriver`

The CQL Engine Flink Driver spins up a Flink job that will consume measure evaluation records from the specified
Kafka topic and generate FHIR MeasureReports.
Each measure evaluation record contains a single patient id and multiple measure ids.
The MeasureReports can either be written to an output Kafka topic, the console, or dropped entirely.

This driver can be run locally, and submitted to a Flink cluster using the shaded jar generated during project
compilation.
The Flink job will run indefinitely until manually canceled.

##### Arguments
* `--kafkaBrokers <ARG>`: The list of Kafka brokers to connect to.
* `--kafkaPassword <ARG>`: The password to connect to Kafka.
* `--kafkaInputTopic <ARG>`: The Kafka topic to read input records from.
* `--kafkaOutputTopic <ARG>` (optional): The Kafka topic to write JSON FHIR MeasureReports to.
    * If not provided, no records will be written to Kafka.
* `--kafkaGroupId <ARG>`: The Kafka consumer group ID.

* `--fhirEndpoint <ARG>`: The HTTP(S) endpoint for the target FHIR server.
* `--fhirUsername <ARG>`: The username for authentication to the FHIR server.
* `--fhirPassword <ARG>`: The password for authentication to the FHIR server.
* `--fhirTenantId <ARG>`: The tenant on the FHIR server to query.

* `--jobName <ARG>` (optional): The name of the submitted Flink Job (Default: `cohort-engine`).
* `--printOutputToConsole` (optional): Log the MeasureReports to standard out (Default: `false`).
* `--rebalanceInput` (optional): Perform a `rebalance` operation after reading the input records from Kafka (Default: `false`).
    * Useful if you want to scale your compute tasks separately from your Kafka topic partitions.
* `--readFromStart` (optional): Start streaming input records from the beginning of the Kafka topic (Default: `false`).

* `--enableCache` (optional): Enable the retrieve cache (Default: `false`).
* `--cacheMaxSize <ARG>` (optional): The maximum size the retrieve cache can grow before evictions begin (Default: `1000`).
* `--cacheExpireOnWrite <ARG>` (optional): The amount of seconds after last write before a retrieve cache entry is evicted (Default: `300`).
* `--enableCacheStatistics` (optional): Enable retrieve cache statistic recording via JMX (Default: `false`).