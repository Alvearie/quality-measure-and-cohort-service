# User Guide - Getting Started

## Accessing the Source Code

Source Code is available under [Quality Measure and Cohort Engine Service](https://github.com/Alvearie/quality-measure-and-cohort-service/) open source project under the [Alvearie](https://github.com/Alvearie/) organization in Github. Builds are published in [Github packages](https://github.com/orgs/Alvearie/packages?repo_name=quality-measure-and-cohort-service) or you can pull the source code and build it yourself. Depending on what mode of operation your are targeting for your solution, you may want to to start with the [cohort-cli](https://github.com/Alvearie/quality-measure-and-cohort-service/packages/506888) and choose the latest shaded jar (more on that below), the [cohort-engine] API JAR, or the [cohort-evaluator-spark] application JAR.

If you are building yourself, use``git clone`` to pull down [the repository](https://github.com/Alvearie/quality-measure-and-cohort-service) and ``mvn install -f cohort-parent`` to build it. If you don't already have Maven installed on your workstation, [download](https://maven.apache.org/download.cgi) version 3.6.3 or newer and follow the [installation instructions](https://maven.apache.org/install.html). You should be using a Java SDK version 8.0 or higher. If you don't already have a Java SDK on your workstation, you can download one [here](https://adoptopenjdk.net/).

When building yourself using the ``mvn install -f cohort-parent`` command, there will be a library JAR produced under ``cohort-engine/target`` and two JARs under ``cohort-cli/target``. The ``cohort-engine/target`` JAR is intended to be consumed by applications that are including the cohort-engine capabilities wrapped up in a larger application. The cohort-cli JARs are for clients that wish to use either of the provided command line interfaces. One of the ``cohort-cli/target`` jars is the command-line interface without dependencies (cohort-cli-VERSION.jar) and the other is an "uber jar" (aka shaded jar) that contains all the project code and dependencies in a single executable artifact (cohort-cli-VERSION-shaded.jar). Choose the artifact that makes the most sense for your execution environment. Simple command-line testing for a client-server mode interaction is easiest with the shaded JAR.

## Learning to write Clinical Quality Language (CQL) queries

CQL is a public specification and there are a number of helpful resources on the web for learning about the language (see below) or you can use the sample CQL definitions under src/test/resources/cql if you don't have your own.

* Language Specification - [http://cql.hl7.org](http://cql.hl7.org)
* eCQI Resource Center CQL Language Education - [https://ecqi.healthit.gov/cql?qt-tabs_cql=2](https://ecqi.healthit.gov/cql?qt-tabs_cql=2)
* CQL Online Library Editor - [http://cql-online.esacinc.com/](http://cql-online.esacinc.com/)
* Electronic Clinical Quality Measure (eCQM) Clinical Quality Language (CQL) Basics Webinar -
[https://www.youtube.com/watch?v=3M8qeQNzXX8](https://www.youtube.com/watch?v=3M8qeQNzXX8)

## Plan your Deployment

The quality-measure-and-cohort-service provides support for a variety of deployment models including client-server, server-only, and big-data/analytics. See the [Deployment Models](user-guide/deployment-models.md) page for details.
