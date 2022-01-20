
# FHIR Resource Tooling

The [fhir-resource-tooling](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/fhir-resource-tooling) module contains utilities that were created to support internal processes within the cohort-and-quality-measure-service team and consumers of our solution inside of IBM. The project produces a shaded JAR `fhir-resource-tooling-VERSION-shaded.jar` that provides a simple command line interface for loading measure, library, and terminology resources to a target FHIR server. Two interfaces are provided - one for measure and library resources from zip archives and another for creating ValueSet resources from spreadsheets that were exported from the National Library of Medicine's [Value Set Authority Center (VSAC)](https://vsac.nlm.nih.gov/) website.

Configuration of FHIR server connection details for both tools follows the same [FHIR server configuration file](user-guide/client-server-guide#fhir-server-configuration) as the cohort-evaluator and measure-evaluator CLI tools described elsewhere in the User Guide. Users should also make sure to provide whatever SSL configuration settings are needed as described in [Secure Socket Layer (SSL) Configuration](user-guide/client-server-guide.md#secure-socket-layer-ssl-configuration) section of the client-server mode documentation.

### Measure ZIP
The canonical way to handle groups of related FHIR resources is to group them together into a FHIR bundle resource, but we find it useful in some some internal processes within our team to keep the resources as separate files and then bundle them as a ZIP that can be imported into the FHIR server. We created a utility that will accept resources in our internal ZIP format, turn them into a FHIR bundle, and then send that bundle to a target FHIR server. The utility reads resources from well-known locations in the provided ZIP archive(s). FHIR resources within the archive are assumed to have no assigned resource ID. The utility will generate a resource ID for each resource based on the assigned name and version fields (the business version, not the FHIR metadata version). The resource ID generation guarantees that any resources that are logically the same business entity will always get the same ID and will be updated on load if they previously exist.

The ZIP archive should be formatted in the following manner...

* fhirResources
* fhirResources/MeasureName-Version.json
* fhirResources/libraries
* fhirResources/libraries/Library1-Version.json
* fhirResources/libraries/Library2-Version.json
* fhirResources/libraries/Library3-Version.json
...

A sample program invocation might look like the following...

```
$> java -jar fhir-resource-tooling-VERSION-shaded.jar measure-importer -m local-fhir-config.json /path/to/measurebundle.zip
```

The FHIR bundles that are generated and the contents server response are written to an output path specified by the `-o` option. If no `-o` option is provided then the output is written to the current working directory. Users are responsible for creating the directory used in the -o option prior to executing the importer. The directory will not be created for you.

### VSAC Spreadsheet
Another of our internal processes involves downloading and importing terminology resources into the FHIR server based on spreadsheets downloaded from the National Library Of Medicine's [ValueSet Authority Center (VSAC)](https://vsac.nlm.nih.gov/) website. In you want to download and import your own ValueSets from VSAC, start at the [VSAC website](https://vsac.nlm.nih.gov/),  navigate to the `Search Value Sets` tab to look for specific value sets, and then select `Export Value Set Results` on relevant sets. Once downloaded, use an invocation similar to the following to import whatever spreadsheets were exported.

```
$> java -jar fhir-resource-tooling-VERSION-shaded.jar value-set-importer -m local-fhir-config.json <list_of_spreadsheet_valuesets>
```
To see an existing template one can use to build one's own value sets from scratch, please check out our [template](user-guide/value_set_template.xlsx ':ignore').


The ValueSetImporter tool can also be used to load value set spreadsheets that contain custom codes from alternate systems. One way to do so would be to simply replace the code system name in the spread sheet for a given code with the full url of that code. If you wish to use custom mappings, simply provide the argument `-c` or `--code-system-mappings` and supply a custom mapping file. To see an example of a custom code system map, click [here](user-guide/custom_codes.txt ':ignore').

```
$> java -jar fhir-resource-tooling-VERSION-SNAPSHOT-shaded.jar value-set-importer -m local-fhir-config.json -c <custom_codes.txt> <list_of_spreadsheet_valuesets>
```

Please note, if you intend to use custom mappings those mappings must be provided to load the value set with this tool.

There is also the ability to write the json representation of the value set either locally or to cos, in the case that you would like to check that it looks how you would expect. In this case, rather than the server configuration, you would need to specify either COS, LOCAL, or BOTH with the --output-locations flag.


#####Parameters

```
-m,--measure-server
The path to the JSON configuration data for the FHIR server connection that will be used to retrieve measure and library resources. If this is provided, `output-locations` must be **NONE**.
--override-existing-value-sets
If provided, will force the creation of valuesets, even if they already exist.
-c,--code-system-mappings 
A file containing custom code system mappings to be used.
--output-locations
Determines if and where to write a FHIR representation of the valueset. If set to **NONE**, the value sets will be written to the specified FHIR database. If **LOCAL**, the program will use the `file-system-output-path` option to determine where to write the FHIR representation locally. If **COS**, the program will use the `cos-configuration` option to determine the specific COS instance and `bucket` to determine which bucket to write it to. If **BOTH**, the program will write the results to both places. If this is not **NONE**, the `measure-server` configuration must not be specified.
-p,--file-system-output-path
The local filesystem path to write results out to (will only be used if output-locations is either **BOTH** or **LOCAL**).
-b,--bucket
COS bucket to write results to (will only be used if output-locations is either **BOTH** or **COS**).
--cos-configuration
A json file containing all the relevant cos configuration needs for access
-o,--file-system-output-format
The format to use when exporting value sets to the file system when using the -p/--file-system-output-path parameters. Valid values are JSON or XML. If not specified, the default output format will be JSON
```
The last argument in the command should be a list of all the spreadsheets you intend to upload.
