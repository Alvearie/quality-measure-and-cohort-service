# FHIR Resource Tooling

This module provides miscellaneous utilities for interacting with FHIR resources and servers including...
* FHIR Server Measure and Library Importer
* FHIR Server Value Set Importer

There is a `FhirResourceToolingCLIDriver` class that delegates to the respective class based on user input.
This class is configured to be the "main class" for the generated shaded jar which facilitates easy execution of all drivers.
