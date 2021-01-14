#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
####################
# First stage:  IBM Java SDK UBI
# IBM Java SDK UBI is not available on public docker yet. Use regular
# base as builder until this is ready. For reference:
# https://github.com/ibmruntimes/ci.docker/tree/master/ibmjava/8/sdk/ubi-min
####################
FROM ibmjava:8-sdk AS builder

WORKDIR /app

# We assume that a maven build has been completed and the docker build is happening
# from the base diretory of the maven project.
COPY --chown=1001:0 cohort-engine-distribution/target/solution /app/cohort-engine-distribution/target/solution
COPY --chown=1001:0 cohort-engine-distribution/target/test /app/cohort-engine-distribution/target/test

# Using a builder image to avoid having to bundle the zip files into the
# final docker image to reduce image size. Unzip in the builder image and
# then later copy the unzipped artifacts to the final image.
RUN mkdir -p /app/cohortSolutionDistribution && \
    mkdir -p /app/cohortTestDistribution && \
    tar -xzf /app/cohort-engine-distribution/target/solution/*.tar.gz -C /app/cohortSolutionDistribution && \
    tar -xzf /app/cohort-engine-distribution/target/test/*.tar.gz -C /app/cohortTestDistribution

####################
# Multi-stage build. New build stage that uses the Liberty UBI as the base image.
# Liberty document reference : https://hub.docker.com/_/websphere-liberty/
####################
FROM us.icr.io/cdt-common-rns/base-images/ubi8-liberty:latest

# The ARG maintainer is expected to be replaced by a value from the KeyProtect
# instance configured for the starter app.
# This integration depends on code in preDockerBuild.sh retrieving the value,
# setting it as an ENV var and REPLACEARGS: "true" being set in the CI: section
# of pipeline.config.
# The keyprotect + replaceargs support is available starting in stable-3.0.2,
# see https://github.ibm.com/whc-toolchain/whc-commons/tree/stable-3.0.4/docs/ready/ci-vault for more information.
#ARG WH_COHORTING_APP_TOOLCHAIN_MAINTAINER=cohortTeamFunctionalIdReplacedByValueInCloudKeyProtect

# Labels - certain labels are required if you want to have
#          a Red Hat certified image (this is not a full set per se)
#LABEL maintainer=${WH_COHORTING_APP_TOOLCHAIN_MAINTAINER}
LABEL maintainer="IBM Quality Measure and Cohort Service Team"
LABEL description="Quality Measure and Cohort Service"
LABEL name="cohorting-app"
LABEL vendor="Alvearie Open Source by IBM"
LABEL version="1.0"
LABEL release="1"
LABEL summary="Quality Measure and Cohort Service"
LABEL description="Quality Measure and Cohort Service available via REST API"

ENV WLP_HOME /opt/ibm/wlp

# create server instance
ENV SERVER_NAME myServer
RUN $WLP_HOME/bin/server create $SERVER_NAME && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/resources/security && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/properties

USER root
# Update image to pick up latest security updates
# Make dir for test resources
# Update symlnk used by Liberty to new server.  Need root.
RUN microdnf update -y && microdnf clean all && \
    mkdir -p /opt/alvearie/cohortTestResources && \
    ln -sfn $WLP_HOME/usr/servers/$SERVER_NAME /config

#Copy in war files, config files, etc. to final image
USER whuser
COPY --from=builder /app/cohortSolutionDistribution/solution/webapps/*.war /config/apps/
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/server.xml /config/
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/jvm.options /config/
COPY --from=builder /app/cohortTestDistribution/ /opt/alvearie/cohortTestResources/

# Copy our startup script into the installed Liberty bin
COPY --from=builder /app/cohortSolutionDistribution/solution/bin/runServer.sh $WLP_HOME/bin/

# Change to root so we can do chmods to our WH user
USER root

# Grant write access to apps folder and startup script
RUN chmod -R u+rwx,g+rx,o+rx $WLP_HOME && \
    chmod -R u+rwx,g+rx,o+rx /opt/alvearie/cohortTestResources

# install any missing features required by server config
RUN $WLP_HOME/bin/installUtility install --acceptLicense $SERVER_NAME

#DEBUG
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/" ]
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/*" ]

USER whuser

# Expose the servers HTTP and HTTPS ports.  NOTE:  must match with hardcoded testcase stage scripts, Helm charts (values.yaml), server.xml
EXPOSE 9080 9443

ENTRYPOINT $WLP_HOME/bin/runServer.sh