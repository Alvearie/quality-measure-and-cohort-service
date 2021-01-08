#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
#FROM openliberty/open-liberty:20.0.0.12-full-java8-openj9-ubi

# Update the image to avoid vulnerabilities
#USER root
#RUN yum update -y --nobest && \
#	yum install -y unzip && \
#	yum clean all -y
#USER 1001

# Add Liberty server configuration including all necessary features
#COPY --chown=1001:0  cohort-engine-api-web/src/main/resources/server.xml /config/

# Add app
#COPY --chown=1001:0  cohort-engine-api-web/target/cohort-engine-api-web-*.war /config/apps/

# This script will add the requested server configurations, apply any interim fixes and populate caches to optimize runtime
#RUN configure.sh


####################
# First stage:  IBM Java SDK UBI
# IBM Java SDK UBI is not available on public docker yet. Use regular
# base as builder until this is ready. For reference:
# https://github.com/ibmruntimes/ci.docker/tree/master/ibmjava/8/sdk/ubi-min
####################
FROM ibmjava:8-sdk AS builder

WORKDIR /app

# uncomment below when we start building with maven
COPY . /app
#COPY --chown=1001:0 cohort-engine-distribution /app/cohort-engine-distribution
#COPY --chown=1001:0 scripts /app/scripts


#uncomment to do the maven build in the docker image build
#you will need to pass in the IBM_USERNAME and IBM_GITHUB_PUBLIC_TOKEN
#so maven can download the dependency packages from the 
#http://github.com/Alvearie/rest-service-framework/ project
#eg docker build --build-arg IBM_USERNAME=<replaceWithUserId> --build-arg IBM_GITHUB_PUBLIC_TOKEN=<replaceWithGithubToken> -t cohort-app-cloud:v1 .
#ARG IBM_USERNAME=beSureToPassIntoDockerBuild
#ARG IBM_GITHUB_PUBLIC_TOKEN=passMeInToo
#ENV IBM_USERNAME=$IBM_USERNAME
#ENV IBM_GITHUB_PUBLIC_TOKEN=$IBM_GITHUB_PUBLIC_TOKEN

# Install maven and maven wrapper and then use it to build our web app
#RUN apt-get update && apt-get install -y maven && \
#    mvn -N io.takari:maven:wrapper -Dmaven=3.5.0 && \
#    ./mvnw clean install -f cohort-parent -s .toolchain.maven.settings.xml

# Install unzip
#RUN apt-get update && apt-get install -y unzip

RUN mkdir -p /app/cohortSolutionDistribution && \
    mkdir -p /app/cohortTestDistribution
#RUN ["/bin/bash", "-c", "unzip -d /app/cohort-engine-distribution/target/solution/*.tar.gz /app/cohortSolutionDistribution" ]
RUN tar -xzf /app/cohort-engine-distribution/target/solution/*.tar.gz -C /app/cohortSolutionDistribution
#RUN ["/bin/bash", "-c", "unzip -d /app/cohort-engine-distribution/target/test/*.tar.gz /app/cohortTestDistribution" ]
RUN tar -xzf /app/cohort-engine-distribution/target/test/*.tar.gz -C /app/cohortTestDistribution
####################
# Multi-stage build. New build stage that uses the Liberty UBI as the base image.
# Liberty document reference : https://hub.docker.com/_/websphere-liberty/
####################
# Original success:  FROM ibmcom/websphere-liberty:20.0.0.3-full-java8-ibmjava-ubi
# List versions and options:   ibmcloud cr images --include-ibm | grep liberty
FROM us.icr.io/cdt-common-rns/base-images/ubi8-liberty:latest

# The ARG maintainer is expected to be replaced by a value from the KeyProtect
# instance configured for the starter app.
# This integration depends on code in preDockerBuild.sh retrieving the value,
# setting it as an ENV var and REPLACEARGS: "true" being set in the CI: section
# of pipeline.config.
# The keyprotect + replaceargs support is available starting in stable-3.0.2,
# see https://github.ibm.com/whc-toolchain/whc-commons/tree/stable-3.0.4/docs/ready/ci-vault for more information.
ARG WH_COHORTING_APP_TOOLCHAIN_MAINTAINER=cohortTeamFunctionalIdReplacedByValueInCloudKeyProtect

# Labels - certain labels are required if you want to have
#          a Red Hat certified image (this is not a full set per se)
LABEL maintainer=${WH_COHORTING_APP_TOOLCHAIN_MAINTAINER}
LABEL description="Quality Measure and Cohort Service"

ENV WLP_HOME /opt/ibm/wlp

# create server instance
ENV SERVER_NAME myServer
RUN $WLP_HOME/bin/server create $SERVER_NAME && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/resources/security && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/properties

USER root
# Install more software
#RUN microdnf update -y && rm -rf /var/cache/yum && \
#    microdnf install -y --nodocs jq sudo zip unzip net-tools which openssh-clients curl openssl rsync gettext procps wget bind-utils iputils && \
#    microdnf clean all

# Update symlnk used by Liberty to new server.  Need root.
RUN ln -sfn $WLP_HOME/usr/servers/$SERVER_NAME /config

# Deploy the application by unpacking into the apps dir for the server. Matches server.xml location tag.
RUN mkdir -p /cohortSolutionDistribution && \
    mkdir -p /cohortTestDistribution
USER whuser

#ADD --from=builder /app/cohort-engine-distribution/target/solution/*.tar.gz /cohortSolutionDistribution
#ADD --from=builder /app/cohort-engine-distribution/target/test/*.tar.gz /cohortTestDistribution
#COPY --from=builder /app/cohort-engine-distribution/target/solution/*.tar.gz /cohortSolutionDistribution
#COPY --from=builder /app/cohort-engine-distribution/target/test/*.tar.gz /cohortTestDistribution
COPY --from=builder /app/cohortSolutionDistribution/solution/webapps/*.war /config/apps
#RUN ["/bin/bash", "-c", "unzip -d /config/apps /config/*.war" ]
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/server.xml /config/
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/jvm.options /config/

# Copy our startup script into the installed Liberty bin
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/runServer.sh $WLP_HOME/bin/

# Change to root so we can do chmods to our WH user
USER root

# Grant write access to apps folder and startup script
RUN chmod -R u+rwx,g+rx,o+rx $WLP_HOME

# install any missing features required by server config
RUN $WLP_HOME/bin/installUtility install --acceptLicense $SERVER_NAME

#DEBUG
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/" ]
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/*" ]

USER whuser

# Expose the servers HTTP and HTTPS ports.  NOTE:  must match with hardcoded testcase stage scripts, Helm charts (values.yaml), server.xml
EXPOSE 9080 9443

ENTRYPOINT $WLP_HOME/bin/runServer.sh