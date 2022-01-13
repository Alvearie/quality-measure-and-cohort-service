# User Guide - Using the Java APIs

We release a `cohort-engine` jar containing public APIs meant to provide developers a way to write applications that
interface with the CQL Engine's underlying logic. This page contains notes on getting your development environment
set up to code against our Java APIs.

We recommend first looking through the [getting started guide](user-guide/getting-started.md) for better understanding the prerequisites and tools being used.

### Available Jars
We publish our jars to [GitHub packages](https://github.com/Alvearie/quality-measure-and-cohort-service/packages/).
Both release jars and snapshot builds are available.
If developing against the snapshot builds, keep in mind that APIs and functionality may be unstable.
We highly recommend developing against the release jars when possible.

### Setting up GitHub Packages
GitHub packages requires authentication when downloading published dependencies. As such, there is additional setup that
needs to be performed before you will be able to download jars for use in your project. These instructions use maven
for configuring and building a project using our Java APIs.

To access dependencies on GitHub packages, you must first create a Personal Access Token by going to https://github.com/settings/tokens and logging in with your GitHub userid. Click "Personal access tokens" in the left side menu, then click the "Generate new token" button. Under the "Select Scopes," check read:packages and click the "Generate Token" button. Copy the generated token string as it will be used in your settings.xml. For an example of settings.xml and an example of where to put your personal access token, please refer to the [getting started](dev-guide/getting-started?id=build-the-code) page of the developers's guide.

Once maven is configured, you should be able to add the `cohort-engine` dependency to your project to have access to
our CQL evaluation entrypoints. Releases of the `cohort-engine` dependency can be found on [GitHub](https://github.com/Alvearie/quality-measure-and-cohort-service/packages/471313/versions).
Clicking on any particular version will show what you need to add to your build file.

[Other published dependencies](https://github.com/orgs/Alvearie/packages?repo_name=quality-measure-and-cohort-service)
can similarly be added to your project.

#### Troubleshooting
If attempting to build your project results in a failure with an `Authorization failed` or a `403` error code, a likely
culprit is the github token mentioned during setup. The recommended solution is to make a new token with
the correct permissions and reconfigure your settings.xml file. Then, attempt to rebuild the project.

## Entrypoints for Java Evaluation
We have two primary Java classes meant to be used during CQL evaluation development: the [CqlEvaluator](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-engine/src/main/java/com/ibm/cohort/engine/CqlEvaluator.java)
and [MeasureEvaluator](https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/cohort-engine/src/main/java/com/ibm/cohort/engine/measure/MeasureEvaluator.java).

Example usage for these classes are available in the `CohortCLI` and `MeasureCLI` (which are each described on
the [getting started](user-guide/getting-started.md) page).
Both of these CLIs are exposed through the entrypoint in the cohort-cli shaded jar produced by our build process.
