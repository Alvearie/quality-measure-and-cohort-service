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

Download [Eclipse IDE for Enterprise Java Developers](https://www.eclipse.org/downloads/packages/release/2020-06/r/eclipse-ide-enterprise-java-developers). This includes Git integration and the M2E plugin which is all that should be needed to work with the project.

## Build the code

Git fork and clone the quality measure and cohort engine repository using GitHub Standard Fork & Pull Request Workflow. For instance, ``git clone git@github.com:<myuser>/Alvearie/quality-measure-and-cohort-service.git`` where ``myuser`` is your GitHub user or GitHub organization.

The code in the quality-measure-and-cohort-service project has dependencies on code in the https://github.com/Alvearie/rest-service-framework project. In order to build, we must set up access to the snapshot packages in the rest-service-framework project. To do this, you must first create a Personal Access Token by going to https://github.com/settings/tokens and logging in with your github userid. Click "Personal access tokens" in the left side menu, then click the "Generate new token" button. Under the "Select Scopes," check read:packages, write:packages and delete:packages and click the "Generate Token" button. Copy the generated token string as it will be used in the settings.xml updates below.

Update your maven ~/.m2/settings.xml to allow your local maven to retrieve the snapshot packages for the rest-service-framework project. Below is an example settings.xml file:


	<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
		<servers>
			<server>
				<id>github</id>
				<username>REPLACE_WITH_YOUR_GITHUB_ID</username>
				<!-- generate at https://github.com/settings/tokens using read:packages, write:packages and delete:packages -->
				<password>REPLACE_WITH_YOUR_PERSONAL_ACCESS_TOKEN</password>
			</server>
		</servers>
		
		<profiles>
			<profile>
				<id>github</id>
				<repositories>
					<repository>
						<id>central</id>
						<url>https://repo1.maven.org/maven2</url>
						<releases><enabled>true</enabled></releases>
						<snapshots><enabled>true</enabled></snapshots>
					</repository>
					<repository>
						<id>github</id>
						<name>GitHub Alvearie Apache Maven Packages</name>
						<url>https://maven.pkg.github.com/Alvearie/rest-service-framework</url>
					</repository>
				</repositories>
			</profile>
		</profiles>
	
		<activeProfiles>
			<activeProfile>github</activeProfile>
		</activeProfiles>
	</settings>

Once cloned locally and your settings.xml has been updated, execute the following from the root of the repository:

1. ``mvn clean install -f cohort-parent``

During development, you may want to check code coverage. Code coverage via Jacoco is part of the default build and reports are available via the Maven site plugin under each project's target/site/jacoco folder or you can see an aggregate report in the reports/target/site/jacoco-aggregate folder.

## Import to the IDE / editor of your choice

The quality measure and cohort engine team is using Eclipse and IntelliJ; feel free to use anything you want.

### Importing to Eclipse

1. Import the Projects using File -> Import -> Maven -> ``Existing Maven Projects`` and M2Eclipse configures the projects (import all projects from the repository). Alternatively, if you are using the GIT integration from inside Eclipse, you can ``Add an existing local Git repository`` and then right-click on the repository and choose ``Import Projects``.




