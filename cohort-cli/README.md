# Cohort CLI

This module contains the CLI drivers for various usecases including...
* Cohort Execution
* Measure Execution
* CQL Translation

There also exists a `CohortCliDriver` class that delegates to the respective class based on user input.
This class is configured to be the "main class" for the generated shaded jar which facilitates easy execution of all drivers.
