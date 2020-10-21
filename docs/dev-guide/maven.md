---
artifactory-team-name: wh-cohort-team
---

Maven
=====
We currently use a set of internal IBM Artifactory Maven repositories to store any java artifacts we create.
For more information on Artifactory itself, see [Artifactory and TaaS](dev-guide/artifactory-and-taas.md).

Local Setup
-----------
Your `~/.m2/settings.xml` file must contain a `<server>` element for the following "ids"...
* `ibm-releases`
* `ibm-snapshots`
* `ibm-all`

The `username` value must be your w3 intranet email address.
The `password` value will be your Artifactory API key, which can be retrieved [here](https://na.artifactory.swg-devops.com/artifactory/webapp/#/profile).
All three servers will have the same username and password.

Here's a barebones `settings.xml` template...
```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <servers>
    <server>
      <username>EMAIL_ADDRESS</username>
      <password>API_TOKEN</password>
      <id>ibm-releases</id>
    </server>
    <server>
      <username>EMAIL_ADDRESS</username>
      <password>API_TOKEN</password>
      <id>ibm-snapshots</id>
    </server>
    <server>
      <username>EMAIL_ADDRESS</username>
      <password>API_TOKEN</password>
      <id>ibm-all</id>
    </server>
  </servers>
</settings>
```
__Note__: You can simply copy the three `<server>` elements and merge them into your existing `settings.xml` file instead of completely replacing the whole thing.

Current Repositories
--------------------
We currently have the following Maven repositories...
* `{{artifactory-team-name}}-snapshot-maven-local`: This is where our snapshot artifacts will be stored.
* `{{artifactory-team-name}}-release-maven-local`: This is where our release artifacts will be stored.
* `{{artifactory-team-name}}-maven-remote`: This is meant to be a proxy for artifacts outside our project.  This can be thought of as an alternative to "Maven Central".
* `{{artifactory-team-name}}-snapshot-maven-virtual`: A union of the local snapshot and remote repositories.
* `{{artifactory-team-name}}-release-maven-virtual`: A union of the local release and remote repositories.
* `{{artifactory-team-name}}-all-maven-virtual`: A union of the snapshot and release virtual repositories.  This is the "catch all" respository.
