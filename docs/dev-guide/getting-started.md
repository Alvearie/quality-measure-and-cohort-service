# Developer Guide - Getting Started

* Prereqs
    * Java 8
    * Maven 3.x
    * Git Tools
    * Eclipse _Optional_

## Prereqs

### Java 8 (or above) 

The quality measure and cohort engine has been tested with OpenJDK8 and OpenJDK11. To install Java on your system, we recommend downloading and installing Java 11 OpenJ9 from [https://adoptopenjdk.net](https://adoptopenjdk.net)

### Maven 3.x (or above)

The quality measure and cohort engine is a multi-module Maven project. Any recent version of Maven should work. The team uses 3.6.3. Download from the [Maven download site](https://maven.apache.org/download.cgi).

### Git Tools

To download the repository or a fork, you should have Git Tools installed. Download and install from [git-scm](https://git-scm.com/downloads).

### Eclipse

Optional: If you want or do use Eclipse.

Download [Eclipse IDE for Enterprise Java Developers](https://www.eclipse.org/downloads/packages/release/2020-06/r/eclipse-ide-enterprise-java-developers) and install the following plugins:

## Build the code

Git fork and clone the quality measure and cohort engine repository using GitHub Standard Fork & Pull Request Workflow. For instance, ``git clone git@github.com:<myuser>/FHIR.git`` where ``myuser`` is your GitHub user or GitHub organization.

Once cloned locally, execute the following from the root of the repository:

1. ``mvn clean install -f cohort-parent``

During development, you may want to check code coverage. Code coverage via Jacoco is part of the default build and reports are available via the Maven site plugin under each project's target/site/jacoco folder or you can see an aggregate report in the reports/target/site/jacoco-aggregate folder.

## Import to the IDE / editor of your choice

The quality measure and cohort engine team is using Eclipse; feel free to use anything you want.

### Importing to Eclipse

1. Import the Projects using File -> Import -> Maven -> ``Existing Maven Projects`` and M2Eclipse configures the projects (import all projects from the repository). Alternatively, if you are using the GIT integration from inside Eclipse, you can ``Add an existing local Git repository`` and then right-click on the repository and choose ``Import Projects``.




