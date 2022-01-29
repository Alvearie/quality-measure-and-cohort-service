
# Big-Data Mode using Apache Spark

While deployment modes such as [client-server](user-guide/client-server-guide.md) and [server-only](user-guide/server-only-guide.md) add flexibility and standards-complaince, these modes are likely not ideal for applications that need to process very large datasets due to the amount of data serialization and transfers required. For solutions wanting to process big data there is a program provided that runs under Apache Spark that brings the processing closer to the data. In this mode, the FHIR data model is eschewed in favor of traditional tabular data used by many analytics applications. Facilities are provided for loading the data from randomly ordered files, aggregated based on a specific execution context (e.g. Patient, Claim, etc.), and then evaluated using any number of CQL inputs. Outputs are written back out as tabular data. This guide describes how to configure and use that application.

## Program assets

The module of interest for users consuming the Apache Spark function is [cohort-evaluator-spark](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/cohort-evaluator-spark) under the main [quality-measure-and-cohort-service](https://github.com/Alvearie/quality-measure-and-cohort-service/) project in Github. Provided inside that project is a sample [Dockerfile](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-evaluator-spark/Dockerfile) that can be built and published to the appropriate place in your execution environment. No publicly published version of the docker image exists at this time.

## Program Execution

The Spark application is built on top of Apache Spark 3.1.2 running on top of Hadoop 3.2 or later. Users wanting to run the application should first [download](https://spark.apache.org/downloads.html) and extract the appropriate Spark distribution to their local system and set a SPARK_HOME environment property that points to the location of the extracted files. Windows users will also need to do some additional configuration of Hadoop for Windows by downloading pre-compiled DLLs from https://github.com/cdarlint/winutils/tree/master/hadoop-3.2.2/ and setting HADOOP_HOME environment variable to location of the downloaded files.

The entry point to the Spark application is the `com.ibm.cohort.cql.spark.SparkCqlEvaluator` class. As shown below, there are a number of program arguments that must be provided to the application to enable proper execution. These required options are prefixed with a "\*" character.

```
Usage: SparkCqlEvaluator [options]
  Options:
    -a, --aggregation-contexts
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
    --disable-column-filter
      Disable CQL-based column filtering. When specified, all columns of the 
      Spark input data are read regardless of whether or not they are needed 
      by the CQL queries being evaluated.
      Default: false
    --disable-result-grouping
      Disable use of CQL parameters to group context results into separate 
      rows 
      Default: false
    -e, --expressions
      One or more expression names, as defined in the context-definitions 
      file, that should be run in this evaluation. Defaults to all 
      expressions. 
      Default: []
    --halt-on-error
      If set, errors during CQL evaluations will cause the program to halt. 
      Otherwise, errors are collected and reported in the program's batch 
      summary file and will not cause the program to halt.
      Default: false
    -h, --help
      Print help text
    --input-format
      Spark SQL format identifier for input files. If not provided, the value 
      of spark.sql.sources.default is used.
  * -i, --input-path
      Key-value pair of resource=URI controlling where Spark should read 
      resources referenced in the context definitions file. 
      Specify multiple files by providing a separate option for each input.
      Syntax: -ikey=value
      Default: {}
  * -j, --jobs
      Filesystem path to the CQL job file
    --key-parameters
      One or more parameter names that should be included in the parameters 
      column for output rows that are generated.
    -l, --library
      One or more library=version key-value pair(s), as defined in the jobs 
      file, that describe the libraries that should be run in this evaluation. 
      Defaults to all libraries. Specify multiple libraries by providing a 
      separate option for each library.
      Syntax: -lkey=value
      Default: {}
  * --metadata-output-path
      Folder where program output metadata (a batch summary file and possible 
      _SUCCESS marker file) will be written.
  * -m, --model-info
      Filesystem path(s) to custom model-info files that may be required for 
      CQL translation.
      Default: []
    --output-format
      Spark SQL format identifier for output files. If not provided, the value 
      of spark.sql.sources.default is used.
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
    -t, --terminology-path
      Filesystem path to the location containing the ValueSet definitions in 
      FHIR XML or JSON format.
    --correlation-id
      This correlation ID will be written with any log messages created by the 
      application and also to the batch summary file that is created
```

The typical mode of invocation is to run the program using the spark-submit script from SPARK_HOME\bin to submit a job to a target Spark cluster. The simplest invocation for a Windows user will look like this...

```
%SPARK_HOME%\bin\spark-submit.cmd --name cohort-evaluator-spark --class com.ibm.cohort.cql.spark.SparkCqlEvaluator local:/opt/spark/jars/cohort-evaluator-spark/target/cohort-evaluator-spark.jar @/path/to/program-args.txt
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

The Spark CQL Evaluator application reads application configuration data from a number of files that work together to describe the desired program behavior. The content for each of those configuration files will be discussed in detail below, but, in summary, the configuration options are the path to a filesystem folder containing files in either CQL or ELM+XML format (`-c`), the path to a CQL modelinfo file in XML format (`-m`), and paths to a context-definitions.json file that describes how Spark data will be grouped prior to CQL evaluation (`-d`) and a cql-jobs file that describes which CQL libraries and expressions will be executed during evaluation (`-j`).

Configuration files are specified using a Hadoop compatible path.
Common usecases include...
* S3 Compatible Storage: `s3a://bucket-name/path/to/file`
* Absolute Local Filesystem Path: `file:///path/to/file`
* Relative Local Filesystem Path: `path/from/working/dir`

With the correct dependencies added to the classpath, other Hadoop compatible filesystems should work as well.

You can also use Persistent Volume Claims (PVCs) to mount your configuration files to the local filesystem. For a Spark cluster running under Kubernetes, this can be done using a Persistent Volume (PV)/Persistent Volume Claim (PVC). In the IBM cloud, we test with a PVC that is backed by a PV that resides in IBM Cloud Object Storage (COS). The IBM Cloud team has an excellent [tutorial](https://www.ibm.com/cloud/blog/kubernetes-persistent-volumes-backed-by-ibm-cloud-object-storage-buckets) that describes how to setup your COS-backed PVC that you should review if you wish to pursue this option.

#### Data-related Options

The actual data that is used for CQL evaluation is accessed via Spark's Hadoop plugin. The sample Dockerfile that is provided for the Spark application installs Spark with the hadoop-aws plugin, so that data can be loaded from any Amazon S3 compatible endpoint, such as, IBM Cloud Object Storage (COS). In order to take advantage of the Hadoop S3 plugin, user's should configure Spark using the `spark.hadoop.fs.s3a.access.key`, `spark.hadoop.fs.s3a.secret.key`, `spark.hadoop.fs.s3a.endpoint` configuration properties. Alternatively, the access key and secret key can be set using the [well-known AWS environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html#envvars-list)  `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`. Internal testing tends to use the environment variable mechanism with key data being set into environment variables based on the contents of a [Kubernetes secret](https://spark.apache.org/docs/latest/running-on-kubernetes.html#secret-management). Either way, the endpoint must be set using the Spark configuration property. If data for your application resides in a different type of backing store, refer to the Spark, Kubernetes, and/or Hadoop documentation on how to configure Hadoop appropriately.

There are potentially many input files needed for the CQL evaluation each representing a different flat data table. The physical location of these files is mapped to a logical data type name using one or more `-i` arguments to the Spark CQL Evaluator application. The `-i` arguments are expressed as datatype=uri pairs. For example, Patient data stored in an S3 endpoint would be expressed as `-i Patient=s3a://my-bucket/path/to/data`. The data type name here is important. This key is used downstream in the `context-definitions.json` file and also must correspond to a data type defined in the CQL modelinfo file.

The data being read can be in any format that Spark understands, but all data is assumed to be provided in a single, consistent format. The default format for Spark is parquet. Users can adjust the input format by setting the `spark.sql.sources.default` configuration property when invoking the Spark application or by using the `--input-format` option to the application. The sample Dockerfile that is provided has support for all the basic formats supported natively by Spark and also Deltalake format. Users that want additional formats, such as Apache Iceberg, can use the `--packages` option to spark submit or extend the Docker image to add the additional needed JARs.

While all types of Spark input formats are supported, we recommend the use of formats that are encoded with a schema (e.g. Parquet) to best utilize the full feature set of the CQL engine.

By default, the program attempts to intelligently select the data needed for CQL evaluation rather than read the entire dataset from Spark. As a fail-safe in case column filtering cannot correctly identify the data requirements for the CQL evaluation, an option is provided `--disable-column-filter` that will cause the application to read all available data from the input sources.

Output is configured almost identically to input except it uses `-o` options and the key in the key=value pairs is the context name of a context aggregation defined in the `context-definitions.json` file. More on that later. Output format is, again, available in any supported spark format as specified by the `--output-format` program option, whatever is configured in the `spark.sql.sources.default` Spark configuration option, or parquet if no other configuration is specified.

Data that is read from Spark is automatically converted to the CQL System typesystem at runtime using logic in the SparkTypeConverter class. Spark datatypes with complex structures such as lists, maps, and rows are not supported at this time.  

CQL defines a number of features that work specifically with terminology data and code values consisting of a code string and possibly code system URI, code system version, and display text (see [Filtering with Terminology](https://cql.hl7.org/02-authorsguide.html#filtering-with-terminology) for an example). To use those features, it is important that the fields of the model that contain code data be defined as the System.Code datatype in the modelinfo file and that the CQL engine be able to fill in the extra code-related fields as needed. Support for this has been made available via metadata fields in the Spark schema. A column can be marked `isCodeCol = true` in the metadata and the column contents will automatically be converted into a System.Code value. Additionally, the metadata may contain `system = my-uri-value` which will assign a default codesystem URI to each System.Code that is created or `systemCol = other-column-name` can be used to retrieve the codesystem URI from another column in the source data. There is also a `displayCol = other-column-name` option that controls setting the display text for a code. No metadata is provided for codesystem version. Best practices established by the CQL specification authors recommend that users write CQL using the tilde (~) operator for code comparison which only considers code and codesystem URI values in the comparison. This is the default behavior of the [Filtering with Terminology](https://cql.hl7.org/02-authorsguide.html#filtering-with-terminology) functionality.

### Context Definitions

The data tables that are used as input to the Spark CQL Evaluator application are assumed to be large, unsorted datasets. The first step to CQL evaluation is loading the datasets and then grouping records together into related data. The CQL specification calls these groupings contexts (e.g. context Patient). Any number of evaluation contexts are supported, but each context causes data to be reread from storage and regrouped. These are potentially very expensive operations, so care should be taken when deciding on the aggregation contexts that will be used.

Aggregation contexts are defined in a JSON-formatted configuration file that we typically call `context-definitions.json` though the name isn't prescribed. In the `context-definitions.json` file, you define a primary data type and all of the relationships to related data that will be needed for CQL evaluation. Each record of the primary data type should be uniquely keyed and the related data types can be joined in using one-to-many or many-to-many semantics. In the one-to-many join scenario, data for a related data type is assumed to have a direct foreign key to the primary data type table. In the many-to-many join scenario, data for a related data type is assumed to be once removed from the primary data type table. Join logic is performed first on an "association" table and then again via a different key value on the true related data type.

Both one-to-many and many-to-many joins may specify an optional `whereClause` field which is used to limit which 
rows are included in the results of a join. For example, we may wish to join the primary data type to a related data
type only where a particular column is equal to a given value. In that case, the join in the context definition could
specify `"whereClause": "a_column = 'a_value'"`. The join would be performed as normal, and then filtered down to
only the cases for which the `whereClause` is true. This feature is meant to cover some advanced use cases and will
likely not be needed for a majority of joins.

An example context-definitions.json file might look like this..
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
        },{
                 "name": "Encounter",
                 "primaryDataType": "Patient",
                 "primaryKeyColumn": "patient_id",
                 "relationships":[{
                     "type": "OneToMany",
                     "relatedDataType": "Encounter",
                     "relatedKeyColumn": "patient_id",
                     "whereClause": "status = 'COMPLETE'"
                 }]
         }]
}
```

The `.contextDefinitions[].name` field is user assigned and will be referenced below in the cql-jobs.json `.evalutions[].contextKey` field. The `primaryDataType`, `associationDataType`, and `relatedDataType` columns must match up with the names of input files provided in the `-i` program options. For example, `"primaryDataType" : "Claim"` would correspond to `-i Claim=file:///path/to/Claim` in the program options.

For the initial pilot implementation of the Spark application, join logic is limited to the one-to-many and many-to-many scenarios described above. More complex join logic involving compound primary keys (more than one column required to uniquely identify a record) or related data beyond a single table relationship (with or without an association table in between) are unsupported. These will come in a future release.

If the user wishes to limit which aggregation contexts are evaluated during an application run, there is a program option `-a` that can be used to specify one or more contextKey values for the aggregation contexts that should be evaluated.

#### Advanced Joins

When using normalized data, you might encounter a need to join on mutiple tables to create the desired context.

Let's consider how we would need to set up a Context Definition to support using standardized valuesets within the OMOP data model.

```plantuml
entity "**person**" {
  + ""person_id"": //integer [PK]//
  --
  ...
}

entity "**observation**" {
  + ""observation_id"": //integer [PK]//
  --
  *""person_id"": //integer [FK]//
  --
  *""observation_concept_id"": //integer [FK]//
  --
  ...
}

entity "**concept**" {
  + ""concept_id"": //integer [PK]//
  --
  *""vocabulary_id"": //character varying(20) [FK]//
  --
  ...
}

entity "**vocabulary**" {
  + ""vocabulary_id"": //character varying(20) [PK]//
  --
  ""vocabulary_version"": //character varying(255) //
  --
  ...
}

"**observation**"   }--  "**person**"
"**observation**"   }--  "**concept**"
"**concept**"       }--  "**vocabulary**"
```

Because we need the triplet of `concept_id`, `vocabulary_id`, and `vocabulary_version` to query the terminology provider,
we have to join across both the `concept` and `vocabulary` tables.

And we would use the `with` field within a `MultiManyToMany` join to represent this relationship:

[MultiManyToMany example ContextDefinition](context-definition/example-multi-many-to-many.json ':include :type=json')


### CQL and ModelInfo
CQL files should be stored in a single folder which is indicated by the `-c` program option. Filenames of files contained in that folder should correspond to the library statement and version specified in the contents of the file. For example, a CQL with `library "SampleLibrary" version '1.0.0'` should be named `SampleLibrary-1.0.0.cql` (or SampleLibrary-1.0.0.xml if already translated to ELM).

When a `cql-jobs.json` evaluation request descriptor specifies a CQL Library for which there is no .xml (aka ELM) file available, the system will look for and attempt to translate the corresponding .cql file from the CQL folder. In these cases, the user should specify a modelinfo file using the `-m` option. The contents of the model info describe the entities and fields contained in the data. The entity names should correspond to the datatypes provided in the `-i` program options. The field names should correspond to the column names in the Spark data. There are sample modelinfo files provided in the `src/test/resources` sub-folders of the cohort-evaluator-spark project.

Like the [configuration parameters](user-guide/spark-user-guide?id=configuration-related-options), the CQL and modelinfo parameters support Hadoop compatible paths.

### Enhanced CQL language features

#### Any Column

In order to support the aggregation of multiple fields within a data model,
we have added the `AnyColumn` (field-name prefix-based matching) and `AnyColumnRegex` (field-name regex-based matching)
as [external functions](https://cql.hl7.org/03-developersguide.html#external-functions).

##### Example Usage
```
using "CustomDataModel" version '1.0.0'
include "CohortHelpers" version '1.0.0'

code "PrimaryCode": 'code1' from "SNOMED"
code "SecondaryCode": 'code2' from "SNOMED"

concept "CustomConcept": {
  "PrimaryCode", "SecondaryCode"
} display 'CustomConcept'

// checks if any of the inlined codes (`code1`, `code2`) matches the data from
// the fields that start with `dx_cd` (`dx_cd`, `dx_cd_02`, `dx_cd_03`) in `TypeA`
define "Prefix Example":
  [A] typeA where
    exists(CohortHelpers.AnyColumn(typeA, 'dx_cd') intersect {'code1', 'code2'})

// checks if any of the codes from `CustomConcept` matches the data from
// the fields matching the `dx_cd_{0,1}[0-9]*` regular expression pattern
// (`dx_cd`, `dx_cd_02`, `dx_cd_03`) in `TypeA`
define "Regex Example":
  [A] typeA where
    exists(CohortHelpers.AnyColumnRegex(typeA, 'dx_cd_{0,1}[0-9]*') intersect "CustomConcept".codes)
```

```
library CohortHelpers version '1.0.0'
using "CustomDataModel" version '1.0.0'

define function AnyColumn(key Choice<Any>, key String) returns List<Any> : external
define function AnyColumnRegex(key Choice<Any>, key String) returns List<Any> : external
```

```xml
<!-- CustomDataModel -->
<typeInfo name="A">
  <element elementType="System.String" name="dx_cd"/>
  <element elementType="System.String" name="dx_cd_02"/>
  <element elementType="System.String" name="dx_cd_03"/>
  ...
</typeInfo>
```

### CQL Job Descriptions

The CQL jobs that will be evaluated by a run of the Spark CQL Evaluator application are stored in a JSON-formatted configuration file that is typically called `cql-jobs.json`. Again, the name isn't prescribed, but the rest of this guide will use the name `cql-jobs.json` for simplicity. The `cql-jobs.json` file lists each CQL Library that will be used, which expressions (aka define statements) within the CQL Library will be evaluated, and any input parameters that will be passed to the CQL evaluation engine to be used by parameterized logic within the library, and a `contextKey` that links the CQL to a particular aggregation context defined in the `context-definitions.json` file. A convenience item is provided for configuring CQL parameters at a global level. This is potentially useful for something like a measurement period that is intended to be constant throughout the evaluation of all libraries and statements being evaluated.

An example `cql-jobs.json` file is shown below...
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
				"IsFemale",
				{"name": "IsMale", "outputColumn": "isMaleResultColumn"}
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

For each aggregation context, a separate output table is created. The storage path for the output table is configured using `-o` options as described above. The output table will contain one or more context records per input context ID with a context ID column (sharing the same name as the field in the model definition), a parameters column, and a column for each CQL expression that is evaluated. The default column name for the CQL expression result is `<Library Name>|<Expression Name>`. The pipe delimiter can be changed using the `--default-output-column-delimiter` program option or users can provide their own column name alongside the expression in the `cql-jobs.json` file. An output column name for an expression is defined using the format `{"name": "<Expression Name>", "outputColumn": "<Output Column Name>"}` in the `expressions` list rather than using a plain string for an expression. If more than one column ends up with the same name, a runtime error will be thrown.

For column data, because output is written to single columns and due to the complexity of serializing arbitrarily-shaped data, there is currently a limitation on the data types that may be produced by a CQL expression that will be written to the output table. List data, map/tuple data, domain objects, etc. are not supported. Expressions should return System.Boolean, System.Integer, System.Decimal, System.String, System.Long, System.Date, or System.DateTime data.

The type information for output columns is read directly from the translated CQL (aka Execution Logical Model / ELM) and requires that the `result-types` [option](https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/cql-to-elm/OVERVIEW.md#usage) is passed to the CQL translator during CQL translation. The Spark CQL Evaluator can do CQL translation inline during program execution and automatically sets this option. Users doing their own CQL translation outside the running application must set the option themselves.

By default Spark is configured to disallow overwrites of data files. However, it can be useful for testing purposes to write to the same data path more than one time. If users wish to use overwrite behavior, they can specify the `--overwrite-output-for-contexts` program option.

Depending on a number of factors including the amount of data in the input, how the input data is organized, and how spark parallelism is configured, output tables might end up with a large amount of partitions yielding a large number of output files. In these cases, it might be desirable to compact data prior to writing it to storage. A program option `-n` is provided that, when specified, will cause output data to be compacted into the specified number of partitions.

### Parameters and row grouping

The parameters column in the program output contains JSON data representing the CQL parameters that were used in the evaluation of the column results. The format of the JSON data is the same as the parameter data in the jobs file. This column allows the results of a single context evaluation to be split into multiple rows of related columns. For example, an input parameter might be `EndDate` for the logic and the desired result is for there to be a separate record in the output for each CQL context + end date.

Row grouping by parameters is enabled by default or, if desired, it can be disabled using the `--disable-result-grouping` flag. When enabled, all parameter values are used as the grouping key. In the case where there are more CQL parameter values than are needed for the grouping key, the `--key-parameters` program argument can be used to limit which parameter values are used as the grouping key. Multiple values can be provided in a comma-separate list or this parameter can be repeated as many times as needed to define the exact set of parameters needed.

With row grouping enabled for a job definition that includes two CQL evaluations for the expression `TestExpression` and two distinct parameter values `V1` and `V2` for a single string parameter `P1`, the grouped output would look something like the following.

|contextID|parameters|TestExpression|
|---------|----------|-------------|
|1|{ "P1": { type: "string", value: "V1" } }|Result1|
|1|{ "P1": { type: "string", value: "V2" } }|Result2|
|...|
|_n_|{ "P1": { type: "string", value: "V1" } }|Result1|
|_n_|{ "P1": { type: "string", value: "V2" } }|Result2|

With row grouping disabled, the jobs definition would need to name each output column differently and then you would end up with something like the following output.

|contextID|parameters|TestExpressionV1|TestExpressionV2|
|---------|----------|-------------|-------------|
|1|{}|Result1|Result2|
|_n_|{}|Result1|Result2|

#### Metadata Output

The Spark program will write out metadata files to the folder specified by the `--metadata-output-path` argument.
If all CQL evaluations finish successfully and without any errors during evaluation, then a blank file named `_SUCCESS`
is written to the specified folder. This marker file can be used by other processes as a way to easily check that the
CQL evaluations performed were all successful.

A batch summary file is also written out to the metadata folder with a name of the format `batch_summary-<SPARK_APPLICATION_ID>`.
This file contains various pieces of information about the performed run. The current contents of the file are:

* `applicationId`: The spark application id. This value will match the application id on a Spark History server (if as history server is in use).
* `startTimeMillis`: The starting timestamp of the Spark job in milliseconds.
* `endTimeMillis`: The ending timestamp of the Spark job in milliseconds.
* `runtimeMillis`: Runtime of the job in milliseconds.
* `jobStatus`: `SUCCESS` if the Spark job finished without errors. `FAIL` otherwise. 
* `totalContexts`: The total number of contexts processed.
* `executionsPerContext`: A map containing an entry of `ContextName -> TotalCqlExecutions` for each context processed.
* `errorList`: If one or more CQL evaluation errors occured during the run, then this field contains an entry per error
               detailing the context name, context id, output column being calculated, and the exception that was encountered.
               If no errors were encountered during the run, the `errorList` is omitted.
* `correlationId`: The user-provided id that was specified at runtime that will preface all of the log lines for this job.

**Note**: The default behavior of the Spark program is to gather any errors that occur during CQL evaluation and report
them in the batch summary file rather than to have the program halt when it hits an error during CQL evaluation.
This behavior can be changed by using the `--halt-on-error` option at runtime. When this option is used, the program
will fail outright if an error is hit during CQL evaluation. This allows programs to "fail fast" if that behavior is
needed rather than waiting until the end of a run to see if there are any errors reported. Note that when the program halts early, the contents of the batch summary file is "best effort" and
due to the way Spark reports information during failures it is possible that contents of the file may be
incomplete or inaccurate. However, any reported errors should still be useful for debugging issues during a run.

### Debugging

Sometimes errors arise during CQL evaluation that can be challenging to debug simply by reading the provided exception message. When this occurs, it is often useful to enable the CQL Engine trace facility using the `--debug` program option. This is most helpful when the `enable-locators` option is specified during CQL translation, so take care to provide that option if you are compiling the CQL yourself. CQL trace messages are written to stdout with no additional configuration options available for controlling output location available at this time.
