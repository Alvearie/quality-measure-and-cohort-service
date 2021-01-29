# Code Coverage

## Locally

During development, you may want to check code coverage. Code coverage via Jacoco is part of the default build and reports are available via the Maven site plugin under each project's target/site/jacoco folder or you can see an aggregate report in the reports/target/site/jacoco-aggregate folder.

## Build

On any github actions build of a branch or PR, check thresholds will fail the build.  You can see threshold info in cohort-parent/pom.xml

## SonarCloud

In addition to local coverage reports, we also have SonarCloud checks running against the `main` branch.  The results may not be identical to the Jacoco reports, but should align sufficiently to be useful.

You can find the results [here](https://sonarcloud.io/code?branch=main&id=Alvearie_quality-measure-and-cohort-service).

