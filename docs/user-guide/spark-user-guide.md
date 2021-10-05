
# CQL Evaluation using Apache Spark

## Overview

The CQL evaluation function provided by the quality-measure-and-cohort-service project can be deployed in a variety of modes depending on the needs of the consuming solution. The most flexible option is a client-server mode of operation where the client performs the CQL evaluation for data that is stored in any remote FHIR server supporting the FHIR REST specification. While flexible, the client-server mode is likely not ideal for applications that need to process very large datasets due to the amount of data serialization and transfers required. For solutions wanting to process big data there is a program provided that runs under Apache Spark. In this mode, the FHIR data model is eschewed in favor of traditional tabular data used by many analytics applications. Facilities are provided for loading the data from randomly ordered files, aggregated based on a specific execution context (e.g. Patient, Claim, etc.), and then evaluated using any number of CQL inputs. Outputs are written back out as tabular data. This guide describes how to configure and use that application.

## Program assets

The cohorting capabilities (aka [quality-measure-and-cohort-service](https://github.com/Alvearie/quality-measure-and-cohort-service/)) are delivered as a public open source project under the Alvearie organization in Github. The module of interest for users consuming the Apache Spark function is  [cohort-evaluator-spark](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/cql-spark-evaluator). Provided inside that project is a sample [Dockerfile](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/cql-spark-evaluator/cohort-evaluator-spark/Dockerfile) that can be built and published to the appropriate place in your execution environment. No publicly published version of the docker image exists at this time.

## Program Execution

The Spark application is built on top of Apache Spark 3.1.2 running on top of Hadoop 3.2 or later. Users wanting to run the application should first [download](https://spark.apache.org/downloads.html) and extract the appropriate Spark distribution to their local system and set a SPARK_HOME environment property that points to the location of the extracted files. Windows users will also need to do some additional configuration of Hadoop for Windows by downloading pre-compiled DLLs from https://github.com/cdarlint/winutils/tree/master/hadoop-3.2.2/ and setting HADOOP_HOME environment variable to location of the downloaded files.

The entry point to the Spark application is the `com.ibm.cohort.cql.spark.SparkCqlEvaluator` class. As shown below, there are a number of program arguments that must be provided to the application to enable proper execution. These required options are prefixed with a "\*" character.

```
Usage: SparkCqlEvaluator [options]
  Options:
    -a, --aggregation
      One or more context names, as defined in the context-definitions file, 
      that should be run in this evaluation. Defaults to all evaluations.
      Default: []
  * -d, --context-definitions
      Filesystem path to the context-definitions file.
  * -c, --cql-path
      Filesystem path to the location containing the CQL libraries referenced 
      in the jobs file.
    --debug
      Enables CQL debug logging
      Default: false
    --default-output-column-delimiter
      Delimiter to use when a result column is named using the default naming 
      rule of `LIBRARY_ID + delimiter + DEFINE_NAME`.
      Default: |
    -e, --expression
      One or more expression names, as defined in the context-definitions 
      file, that should be run in this evaluation. Defaults to all 
      expressions. 
      Default: []
    -h, --help
      Print help text
    --input-format
      Spark SQL format identifier for input files. If not provided, the value 
      of spark.sql.datasources.default is used.
  * -i, --input-path
      Key-value pair of resource=URI controlling where Spark should read 
      resources referenced in the context definitions file will be read from. 
      Specify multiple files by providing a separate option for each input.
      Syntax: -ikey=value
      Default: {}
  * -j, --jobs
      Filesystem path to the CQL job file
    -l, --library
      One or more library=version key-value pair(s), as defined in the jobs 
      file, that describe the libraries that should be run in this evaluation. 
      Defaults to all libraries. Specify multiple libraries by providing a 
      separate option for each library.
      Syntax: -lkey=value
      Default: {}
  * -m, --model-info
      Filesystem path(s) to custom model-info files that may be required for 
      CQL translation.
      Default: []
    --output-format
      Spark SQL format identifier for output files. If not provided, the value 
      of spark.sql.datasources.default is used.
    -n, --output-partitions
      Number of partitions to use when storing data
  * -o, --output-path
      Key-value pair of context=URI controlling where Spark should write the 
      results of CQL evaluation requests. Specify multiple files by providing 
      a separate option for each output.
      Syntax: -okey=value
      Default: {}
    --overwrite-output-for-contexts
      WARNING: NOT RECOMMENDED FOR PRODUCTION USE. If option is set, program 
      overwrites existing output when writing result data.
      Default: false
```

The typical mode of invocation is to run the program using the spark-submit script from SPARK_HOME\bin to submit to a target Spark cluster. The simplest invocation for a Windows user will look like this...

```
%SPARK_HOME%\bin\spark-submit.cmd --name cohort-evaluator-spark --class com.ibm.cohort.cql.spark.SparkCqlEvaluator local:/opt/spark/jars/cohort-evaluator-spark/target/cohort-evaluator-spark-1.0.2-SNAPSHOT.jar @/path/to/program-args.txt
```

In this example, the `@/path/to/program-args.txt` is a convenience option that allows users to store their program arguments in a file that can be reused across multiple program executions. Each line of the target file is a separate positional parameter. The option name and option value (for those options that have a value) should be on separate lines. Make sure to avoid trailing whitespace as that can confuse the argument parser. Alternatively, you can just pass all the options (e.g. -c /path/to/cql -m /path/to/modelinfo.xml -d /path/to/context-definitions.json -j /path/to/cql-jobs.json ...) directly in the spark-submit execution.

An example set of program arguments assuming you are running from the cohort-evaluator-spark and using one of the provided unit test examples is shown below. You will need to replace %CWD% with the appropriate directory for your local version of the cohort-evaluator-spark project as Hadoop does not support relative file paths.

```
-d 
src/test/resources/simple-job/context-definitions.json
-j 
src/test/resources/simple-job/cql-jobs.json
-c 
src/test/resources/simple-job/cql
-m 
src/test/resources/simple-job/modelinfo/simple-modelinfo-1.0.0.xml
--input-format
delta
-i
Patient=file://%CWD%/src/test/resources/simple-job/testdata/patient
--output-format
delta
-o
Patient=file://%CWD%/target/patient_cohort
--overwrite-output-for-contexts
```

Spark configuration itself can be quite complex and there are a number of configuration options assumed to be in place for the above example to function. These properties could be specified as `--conf` arguments to spark-submit or our preference is to store these in a separate properties file such as SPARK_HOME\conf\spark-defaults.conf. This guide makes no attempt to cover the complexity of Spark configuration. Necessary properties will vary based on the deployment environment and application needs. For reference, the full set of Spark properties available can be found [here](https://spark.apache.org/docs/latest/configuration.html#spark-configuration). Details on how to configure Spark to run on Kubernetes can be found [here](https://spark.apache.org/docs/latest/running-on-kubernetes.html). 

Application consumers should, at minimum, pay close attention to the following properties:

* spark.master
* spark.driver.memory
* spark.driver.memoryOverhead
* spark.executor.memory
* spark.executor.memoryOverhead

Users running Spark under Kubernetes will probably also need at least the following options:

* spark.kubernetes.namespace
* spark.kubernetes.authenticate.driver.serviceAccountName
* spark.kubernetes.container.image

#### Configuration-related Options

The Spark CQL Evaluator application reads application configuration data from a number of files that work together to describe the desired program behavior. The content for each of those configuration files will be discussed in detail below, but, in summary, the configuration options are the path to a filesystem folder containing files in either CQL or ELM+XML format (`-c`), the path to a CQL modelinfo file in XML format (`-m`), and paths to a context-definitions.json file that describes how Spark data will be grouped prior to CQL evaluation (`-d`) and a cql-jobs file that describes which CQL libraries and expressions will be executed during evaluation (`-j`). All *configuration data* is assumed to be available in the local filesystem where the Spark application is running. For a Spark cluster running under Kubernetes, this can be done using a Persistent Volume (PV)/Persistent Volume Claim (PVC). In the IBM cloud, we test with a PVC that is backed by a PV that resides in IBM Cloud Object Storage (COS). The IBM Cloud team has an excellent [tutorial](https://www.ibm.com/cloud/blog/kubernetes-persistent-volumes-backed-by-ibm-cloud-object-storage-buckets) that describes how to setup your COS-backed PVC that you should review if you wish to pursue this option.

#### Data-related Options

The actual data that is used for CQL evaluation is accessed via Spark's Hadoop plugin. The sample Dockerfile that is provided for the Spark application installs Spark with the hadoop-aws plugin, so that data can be loaded from any Amazon S3 compatible endpoint, such as, IBM Cloud Object Storage (COS). In order to take advantage of the Hadoop S3 plugin, user's should configure Spark using the `spark.hadoop.fs.s3a.access.key`, `spark.hadoop.fs.s3a.secret.key`, `spark.hadoop.fs.s3a.endpoint` configuration properties. Alternatively, the access key and secret key can be set using the [well-known AWS environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html#envvars-list)  `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`. Internal testing tends to use the environment variable mechanism with key data being set into environment variables based on the contents of a [Kubernetes secret](https://spark.apache.org/docs/latest/running-on-kubernetes.html#secret-management). Either way, the endpoint must be set using the Spark configuration property. If data for your application resides in a different type of backing store, refer to the Spark, Kubernetes, and/or Hadoop documentation on how to configure Hadoop appropriately.

There are potentially many input files needed for the CQL evaluation each representing a different flat data table. The physical location of these files is mapped to a logical data type name using one or more `-i` arguments to the Spark CQL Evaluator application. The `-i` arguments are expressed as datatype=uri pairs. For example, Patient data stored in an S3 endpoint would be expressed as `-i Patient=s3a://my-bucket/path/to/data`. The data type name here is important. This key is used downstream in the `context-definitions.json` file and also must correspond to a data type defined in the CQL modelinfo file. 

The data being read can be in any format that Spark understands, but all data is assumed to be provided in a single, consistent format. The default format for Spark is parquet. Users can adjust the input format by setting the `spark.sql.datasources.default` configuration property when invoking the Spark application or by using the `--input-format` option to the application. The sample Dockerfile that is provided has support for all the basic formats supported natively by Spark and also Deltalake format. Users that want additional formats, such as Apache Iceberg, can use the `--packages` option to spark submit or extend the Docker image to add the additional needed JARs.

Output is configured almost identically to input except it uses `-o` options and the key in the key=value pairs is the context name of a context aggregation defined in the `context-definitions.json` file. More on that later. Output format is, again, available in any supported spark format as specified by the `--output-format` program option, whatever is configured in the `spark.sql.sources.default` Spark configuration option, or parquet if no other configuration is specified.

Data that is read from Spark is automatically converted to the CQL System typesystem at runtime using logic in the SparkTypeConverter class. Spark datatypes with complex structures such as lists, maps, and rows are not supported at this time.  

CQL defines a number of features that work specifically with terminology data and code values consisting of a code string and possibly code system URI, code system version, and display text (see [Filtering with Terminology](https://cql.hl7.org/02-authorsguide.html#filtering-with-terminology) for an example). To use those features, it is important that the fields of the model that contain code data be defined as the System.Code datatype in the modelinfo file and that the CQL engine be able to fill in the extra code-related fields as needed. Support for this has been made available via metadata fields in the Spark schema. A column can be marked `isCodeCol = true` in the metadata and the column contents will automatically be converted into a System.Code value. Additionally, the metadata may contain `system = my-uri-value` which will assign a default codesystem URI to each System.Code that is created or `systemCol = other-column-name` can be used to retrieve the codesystem URI from another column in the source data. There is also a `displayCol = other-column-name` option that controls setting the display text for a code. No metadata is provided for codesystem version. Best practices established by the CQL specification authors recommend that users write CQL using the tilde (~) operator for code comparison which only considers code and codesystem URI values in the comparison. This is the default behavior of the [Filtering with Terminology](https://cql.hl7.org/02-authorsguide.html#filtering-with-terminology) functionality.

### Context Definitions

The data tables that are used as input to the Spark CQL Evaluator application are are assumed to be large, unsorted datasets. The first step to CQL evaluation is loading the datasets and then grouping records together into related data. The CQL specification calls these groupings contexts (e.g. context Patient). Any number of evaluation contexts are supported, but each context causes data to be reread from storage and regrouped. These are potentially very expensive operations, so care should be taken when deciding on the aggregation contexts that will be used.

Aggregation contexts are defined in a JSON-formatted configuration file that we typically call `context-definitions.json` though the name isn't prescribed. In the `context-definitions.json` file, you define a primary data type and all of the relationships to related data that will be needed for CQL evaluation. Each record of the primary data type should be uniquely keyed and the related data types can be joined in using one-to-many or many-to-many semantics. In the one-to-many join scenario, data for a related data type is assumed to have a direct foreign key to the primary data type table. In the many-to-many join scenario, data for a related data type is assumed to be once removed from the primary data type table. Join logic is performed first on an "association" table and then again via a different key value on the true related data type. 

```json
{
        "contextDefinitions": [{
                "name": "Patient",
                "primaryDataType": "Patient",
                "primaryKeyColumn": "patient_id",
				"relationships":[{
					"type": "OneToMany",
					"relatedDataType": "Observation",
					"relatedKeyColumn": "patient_id"
				},{
					"type": "OneToMany",
					"relatedDataType": "Condition",
					"relatedKeyColumn": "patient_id"
				}]
        },{
                "name": "Claim",
                "primaryDataType": "Claim",
                "primaryKeyColumn": "claim_id",
				"relationships": [{
					"type": "ManyToMany",
					"associationDataType": "PatientClaim",
					"associationOneKeyColumn": "claim_id",
					"associationManyKeyColumn": "patient_id",
					"relatedDataType": "Patient",
					"relatedKeyColumn": "patient_id"
				}]
        }]
}
```

The `.contextDefinitions[].name` field is user assigned and will be referenced below in the cql-jobs.json `.evalutions[].contextKey` field. The `primaryDataType`, `associationDataType`, and `relatedDataType` columns must match up with the names of input files provided in the `-i` program options. For example, `"primaryDataType" : "Claim"` would correspond to `-i Claim=file:///path/to/Claim` in the program options.

For the initial pilot implementation of the Spark application, join logic is limited to the one-to-many and many-to-many scenarios described above. More complex join logic involving compound primary keys (more than one column required to uniquely identify a record) or related data beyond a single table relationship (with or without an association table in between) are unsupported. These will come in a future release.

If the user wishes to limit which aggregation contexts are evaluated during an application run, there is a program option `-a` that can be specified one or more times that lists the contextKey value for each aggregation context that should be evaluated.

### CQL and ModelInfo

CQL files should be stored in a single filesystem folder which is indicated by the `-c` program option. Filenames of files contained in that folder should correspond to the library statement and version specified in the contents of the file. For example, a CQL with `library "SampleLibrary" version '1.0.0'` should be named `SampleLibrary-1.0.0.cql` (or SampleLibrary-1.0.0.xml if already translated to ELM) on disk. 

When a `cql-jobs.json` evaluation request descriptor specifies a CQL Library for which there is no .xml (aka ELM) file available, the system will look for and attempt to translate the corresponding .cql file from the CQL folder. In these cases, the user should specify a modelinfo file using the `-m` option. The contents of the model info describe the entities and fields contained in the data. The entity names should correspond to the datatypes provided in the `-i` program options. The field names should correspond to the column names in the Spark data. There are sample modelinfo files provided in the src/test/resources sub-folders of the cohort-evaluator-spark project.

### CQL Job Descriptions

The CQL jobs that will be evaluated by a run of the Spark CQL Evaluator application are stored in a JSON-formatted configuration file that is typically called cql-jobs.json. Again, the name isn't prescribed, but the rest of this guide will use the name cql-jobs.json for simplicity. The cql-jobs.json file lists each CQL Library that will be used, which expressions (aka define statements) within the CQL Library will be evaluated, and any input parameters that will be passed to the CQL evaluation engine to be used by parameterized logic within the library, and a `contextKey` that links the CQL to a particular aggregation context defined in the `context-definitions.json` file. A convenience item is provided for configuring CQL parameters at a global level. This is potentially useful for something like a measurement period that is intended to be constant throughout the evaluation of all libraries and statements being evaluated.

An example cql-jobs.json file is show below...
```json
{
	"globalParameters": {
		"Measurement Period": {
			"type": "interval",
			"start": {
				"type": "date",
				"value": "2020-01-01"
			},
			"end": {
				"type": "date",
				"value": "2021-01-01"
			}
		}
	},
	"evaluations": [
		{
			"descriptor": {
				"libraryId": "SampleLibrary",
				"version": "1.0.0"
			},
			"expressions": [
				"IsFemale"
			],
			"parameters": {
				"MinimumAge": {
					"type": "integer",
					"value": 17
				}
			},
			"contextKey": "Patient",
			"contextValue": "NA"
		}
	]
}
```

Parameters can be expressed as any type on the CQL System typesystem. The types available and the encoding format for the more complex datatypes is described in the [Parameter Formats](https://alvearie.io/quality-measure-and-cohort-service/#/user-guide/parameter-formats) page of the user guide. 

The `.evaluations[].contextKey` value must match one of the `.contextDefinitions[].name` values from the context-definitions.json file. Only the evaluations matching the current context will be run for each aggregation.

If the user wishes to limit which jobs are evaluated during a specific application run, there are program options available that limit which libraries (`-l`) and expressions (`-e`) are evaluated. For the purposes of job filtering, cql library version is ignored. If only the `-e` option is provided, then all expressions with that name will be evaluated across all cql job definitions. 

### Program Output

For each aggregation context, a separate output table is created. The storage path for the output table is configured using `-o` options as described above. The records of the context output table consist of the unique key for the specific context record (sharing the same name as the field in the model definition) and a column for each CQL expression that is evaluated. The default column name for the CQL evaluation columns is "<Library Name>|<Expression Name>". The pipe delimiter can be changed using the `--default-output-column-delimiter` program option or users can provide their own column name alongside the expression in the `cql-jobs.json` file. If more than one column ends up with the same name, a runtime error will be thrown.

Because output is written to single columns and due to the complexity of serializing arbitrarily-shaped data, there is currently a limitation on the data types that may be produced by a CQL expression that will be written to the output table. List data, map/tuple data, domain objects, etc. are not supported. Expressions should return System.Boolean, System.Integer, System.Decimal, System.String, System.Long, System.Date, or System.DateTime data. 

The type information for output columns is read directly from the transpiled CQL (aka Execution Logical Model / ELM) and require that the `result-types` [option](https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/cql-to-elm/OVERVIEW.md#usage) is passed to the CQL translator during CQL translation. The Spark CQL Evaluator can do CQL translation inline during program execution and automatically sets this option. Users doing their own CQL compilation outside the running application must set the option themselves.

By default Spark is configured to disallow overwrites of data files. However, it can be useful for testing purposes to write to the same data path more than one time. If users wish to use overwrite behavior, they can specify the `--overwrite-output-for-contexts` program option.

Depending on a number of factors including the amount of data in the input, how the input data is organized, and how spark parallelism is configured, output tables might end up with a large amount of partitions yielding a large number of output files. In these cases, it might be desirable to compact data prior to writing it to storage. A program option `-n` is provided that, when specified, will cause output data to be compacted into the specified number of partitions.

### Debugging

Sometimes errors arise during CQL evaluation that can be challenging to debug simply by reading the provided exception message. When this occurs, it is often useful to enable the CQL Engine trace facility using the `--debug` program option. This is most helpful when the `enable-locators` option is specified during CQL translation, so take care to provide that option if you are compiling the CQL yourself. CQL trace messages are written to stdout with no additional configuration options available for controlling output location available at this time.
