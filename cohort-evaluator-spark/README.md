# Cohort Evaluator Spark

This project contains a Spark Job that encapsulates reading data from flat files, organizing the data into context groupings, and then executing Clinical Quality Language (CQL) queries against those context groups. A single output is generated for each context grouping where the columns of the result record are the result of each inidivual CQL expression evaluated. The program is intelligent enough to translate raw CQL to ELM provided an appropriate model info artifact is provided.

Context groupings are described using a context-defintions.json file and the CQL libraries to evaluate, with input parameters, are described in a cql-jobs.json file. See src/test/resources for examples.

## Building

This project is built by the main IBM Cloud Toolchain which produces a docker image for each Dockerfile in the repository.

## Running

Once the project has been sucessfully built (e.g. `mvn clean install`), a sample Spark job submission is provided to test the job under the IBM Cloud Kubernetes Service. You must first download and install an appropriate Apache Spark distribution (see https://spark.apache.org/releases/spark-release-3-1-2.html) and then set an environment variable SPARK_HOME that points to the root of the install. From this project, you can then execute `mvn exec:java` to run the sample Spark submission.

The example job assumes that there is configuration data mounted into the Kubernetes driver and executor pods from a Kubernetes Persistent Volume Claim (PVC). The test cluster is configured with the cohort-config PVC backed by a correspondingly named bucket in Cloud Object Store (COS). There are example templates for creating these resources under kubernetes-setup. The data for the example job is assumed to be read from COS directly via hadoop and the AWS client. This is configured by mapping credentials from a Kubernetes secret into well-known environment variables names within the Spark driver and executor pods. 

There are some Maven properties provided that can be used to override resource and file names as needed. See the pom.xml for full details.
