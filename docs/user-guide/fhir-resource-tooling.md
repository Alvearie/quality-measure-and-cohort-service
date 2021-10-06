
# FHIR Resource Tooling

There is a tooling project provided for loading necessary resources into a target FHIR server. The project is fhir-resource-tooling and it produces a shaded JAR ```fhir-resource-tooling-VERSION-shaded.jar``` that provides a simple command line interface for loading the resources. The JAR provides two tools, one for loading resources exported from the Measure Authoring Tool (zip files), and one for loading VSAC Value Set spreadsheets as downloaded from https://cts.nlm.nih.gov/fhir.

Configuration of the importer tooling uses the same [FHIR server configuration file](user-guide/client-server-guide#fhir-server-configuration) as the cohort-evaluator and measure-evaluator CLI tools described above. Users should also make sure to provide whatever SSL configuration settings are needed as described in [Secure Socket Layer (SSL) Configuration](user-guide/client-server-guide.md#secure-socket-layer-ssl-configuration) section of the client-server mode guide.

### Measure ZIP
To load Measure and Library resources exported from the Measure Authoring Tool (zip files) into a target FHIR server, use the following invocation:

```
$> java -jar fhir-resource-tooling-VERSION-shaded.jar -m local-fhir-config.json /path/to/measurebundle.zip
```

Requests are processed as FHIR bundles and the contents of the request and response bundles are written to an output path specified by the ``-o`` option. If no ``-o`` option is provided then the output is written to the current working directory. Users are responsible for creating the directory used in the -o option prior to executing the importer. The directory will not be created for you.

Archive contents should contain the following folders and resources...

* fhirResources
* fhirResources/MeasureName-Version.json
* fhirResources/libraries
* fhirResources/libraries/Library1-Version.json
* fhirResources/libraries/Library2-Version.json
* fhirResources/libraries/Library3-Version.json
...

The Resource ID of each included resource is assumed to be empty. Each resource's target resource ID is calculated based on the resource name and version so that the same business identifiers always refer to the same FHIR resource in the target server.

### VSAC Spreadsheet
In order to use this tool, you will need to download spreadsheets from the VSAC website. Start at the VSAC authority website [here](https://vsac.nlm.nih.gov/). Navigate to the Search Value Sets tab to look for specific value sets, and select Export Value Set Results on relevent sets.
<br>To load value set resources downloaded from the NIH website (spreadsheets, not zip files) into a target FHIR server, use the following invocation:

```
$> java -cp fhir-resource-tooling-VERSION-shaded.jar com.ibm.cohort.tooling.fhir.ValueSetImporter -m local-fhir-config.json <list_of_spreadsheet_valuesets>
```
To see an existing template one can use to build one's own value sets from scratch, please check out our [template](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/docs/user-guide/value_set_template.xlsx). <br> <br>
The ValueSetImporter tool can also be used to load value set spreadsheets that contain custom codes from alternate systems. One way to do so would be to simply replace the code system name in the spread sheet for a given code with the full url of that code. If you wish to use custom mappings, simply provide the argument `-c` or `--code-system-mappings` and supply a custom mapping file. To see an example of a custom code system map, click [here](https://github.com/Alvearie/quality-measure-and-cohort-service/tree/main/docs/user-guide/custom_codes.txt).<br> 
```
$> java -cp fhir-resource-tooling-0.0.1-SNAPSHOT-shaded.jar com.ibm.cohort.tooling.fhir.ValueSetImporter -m local-fhir-config.json -c <custom_codes.txt> <list_of_spreadsheet_valuesets>
```

Please note, if you intend to use custom mappings those mappings must be provided to load the value set with this tool.
