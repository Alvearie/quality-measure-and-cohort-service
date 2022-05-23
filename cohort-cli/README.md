# Cohort CLI

This module contains the CLI drivers for various usecases including...
* Cohort Execution
* Measure Execution
* CQL Translation

There is a `CohortCliDriver` class that delegates to the respective driver based on user input.
This class is configured to be the "main class" for the generated shaded jar which facilitates easy execution of all drivers.
