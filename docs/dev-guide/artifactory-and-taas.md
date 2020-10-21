Artifactory and TaaS
====================
This page will cover how we can use and manage our Artifactory repos.

TaaS
----
The [TaaS](https://self-service.taas.cloud.ibm.com/) organization aims to provide a simple way to provision and manage commonly used resources not handled by other organizations (e.g. IBM Whitewater).
Before doing anything with TaaS, you need to be a part of or create a team.
Your current teams, and the ability to create a new team can be found [here](https://self-service.taas.cloud.ibm.com/teams).

For this project, we are operating under the [wh-cohort-team](https://self-service.taas.cloud.ibm.com/teams/wh-cohort-team) team.

### Managing Users
You can add new users to a TaaS team by scrolling down to the `Team Membership` section of your team's page.
Adding new users here will give them access to the TaaS resource for our team, but will not give access to any of the resources managed by TaaS (e.g. Artifactory).

**Note**: Giving access to our TaaS team is *not* required to give someone access to our Artifactory repositories.

**Note**: We have not identified a set of "admins" for our TaaS team yet.
We're currently giving every user the `admin` role.
This is subject to change as our usage of TaaS matures.

### Managing Subscriptions
You can manage what subscriptions your team has by scrolling down to the `Subscriptions` section of your team's page.
You first need a subscription before you can create any resources.
We currently have no need for any subscriptions past the `Core Subscription`, which gives us "one dedicated Jenkins Master, one Artifactory team, and one UCD team".

### Managing Resources
You can manage the resources utilized by your team by scrolling down to the `Resources` section of your team's page.
We currently only have an Artifactory resource setup.
Further resources may get created later, but we have not seen a need for them yet.
Clicking the `Manage Resource` button for the Artifactory resource will take you to the management page for our Artifactory repos.

Artifactory
----------
The TaaS organization has provisioned a massive [Artifactory](https://na.artifactory.swg-devops.com/artifactory) instance for all TaaS customers to use.
By creating a new Artifactory "resource", you're actually just gaining access to create repositories under the single Artifactory instance.
Any number of "repositories" may be created under our Artifactory resource.
There's a [wide selection](https://www.jfrog.com/confluence/display/JFROG/Package+Management) of potential repository types to choose from.

For this project, we have the [wh-cohort-team-artifactory](https://self-service.taas.cloud.ibm.com/teams/wh-cohort-team/wh-cohort-team-artifactory) resource.

### Managing Users
User access for Artifactory is managed separately from the TaaS team itself.
You can manage the users for an Artifactory resource by scrolling down to the `Roles` section of your team's Artifactory page.
Giving users access to our Artifactory resource will allow them to browse and potentially create new artifacts and repositories.

**Note**: Developers on the cohorting project should be given `write/read` access, but outside developers should only need `read only` access.

### Managing Repositories
You can managed the repositories for an Artifactory resource by scrolling down to the `Repositories` section of your team's Artifactory page.
Here you can create or edit repositories as needed.  The different types of respositories and how they can be configured is outside the scope of this document.
