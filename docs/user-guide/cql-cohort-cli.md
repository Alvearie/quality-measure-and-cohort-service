# CQL Cohort CLI

The `cohort-cli` shaded jar includes a command-line utility for executing CQL files.

```
Usage: cohort-cli [options]
  Options:
    -c, --context-id
      Unique ID for one or more context objects (e.g. Patient IDs)
    -n, --context-name
      Context Object Name
      Default: Patient
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
      'ELM' Expression(s) to Execute
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
      Default: NONE
      Possible Values: [NONE, DEBUG, TRACE]
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
      Default: 'ELM'
      Possible Values: [CQL, ELM]
    --summarize-collections
      If set, collection sizes will be displayed in the CLI output instead of 
      collection contents.
      Default: false
    -t, --terminology-server
      Path to JSON configuration data for the FHIR server connection that will 
      be used to retrieve terminology.
```

!>
Extra caution should be taken when overriding the default `--context-name`. <br>
There are a few non-`FHIR` context names that are valid as a part of the [DBCG implementation](https://github.com/DBCG/cql_engine/blob/v1.5.4/engine.fhir/src/main/java/org/opencds/cqf/cql/engine/fhir/model/FhirModelResolver.java#L93): <br>
`Population`, `Unfiltered`, and `Unspecified`. <br>
In general, however, this parameter needs to be a valid `FHIR` resource. <br>
