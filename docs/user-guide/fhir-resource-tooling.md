
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
$> java -jar fhir-resource-tooling-VERSION-shaded.jar -m local-fhir-config.json /path/to/measurebundle.zip
```

The FHIR bundles that are generated and the contents server response are written to an output path specified by the `-o` option. If no `-o` option is provided then the output is written to the current working directory. Users are responsible for creating the directory used in the -o option prior to executing the importer. The directory will not be created for you.

### VSAC Spreadsheet
Another of our internal processes involves downloading and importing terminology resources into the FHIR server based on spreadsheets downloaded from the National Library Of Medicine's [ValueSet Authority Center (VSAC)](https://vsac.nlm.nih.gov/) website. In you want to download and import your own ValueSets from VSAC, start at the [VSAC website](https://vsac.nlm.nih.gov/),  navigate to the `Search Value Sets` tab to look for specific value sets, and then select `Export Value Set Results` on relevant sets. Once downloaded, use an invocation similar to the following to import whatever spreadsheets were exported.

```
$> java -cp fhir-resource-tooling-VERSION-shaded.jar com.ibm.cohort.tooling.fhir.ValueSetImporter -m local-fhir-config.json <list_of_spreadsheet_valuesets>
```
To see an existing template one can use to build one's own value sets from scratch, please check out our [template](user-guide/value_set_template.xlsx ':ignore').


The ValueSetImporter tool can also be used to load value set spreadsheets that contain custom codes from alternate systems. One way to do so would be to simply replace the code system name in the spread sheet for a given code with the full url of that code. If you wish to use custom mappings, simply provide the argument `-c` or `--code-system-mappings` and supply a custom mapping file. To see an example of a custom code system map, click [here](user-guide/custom_codes.txt ':ignore').

```
$> java -cp fhir-resource-tooling-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.tooling.fhir.ValueSetImporter -m local-fhir-config.json -c <custom_codes.txt> <list_of_spreadsheet_valuesets>
```

Please note, if you intend to use custom mappings those mappings must be provided to load the value set with this tool.
