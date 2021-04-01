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
FROM us.icr.io/cdt-common-rns/base-images/ubi8:latest AS builder

WORKDIR /app
ENV COHORT_DIST_SOLUTION=/app/cohortSolutionDistribution \
    ALVEARIE_HOME=/opt/alvearie

# We assume that a maven build has been completed and the docker build
# is happening from the base diretory of the maven project.
COPY cohort-engine-distribution/target/solution /app/cohort-engine-distribution/target/solution

# Using a builder image to avoid having to bundle the zip files into the
# final docker image to reduce image size. Unzip in the builder image and
# then later copy the unzipped artifacts to the final image.
RUN mkdir -p $COHORT_DIST_SOLUTION && \
    tar -xzf /app/cohort-engine-distribution/target/solution/*.tar.gz -C $COHORT_DIST_SOLUTION

####################
# Multi-stage build. New build stage that uses the Liberty UBI as the base image.
# Liberty document reference : https://hub.docker.com/_/websphere-liberty/
####################
#TODO periodically update to the latest base image
FROM us.icr.io/cdt-common-rns/base-images/ubi8-liberty:20210308.1322

# Labels - certain labels are required if you want to have
#          a Red Hat certified image (this is not a full set per se)
LABEL maintainer="IBM Quality Measure and Cohort Service Team" \
      description="Quality Measure and Cohort Service" \
      name="cohorting-app" \
      vendor="Alvearie Open Source by IBM" \
      version="1.0" \
      release="1" \
      summary="Quality Measure and Cohort Service" \
      description="Quality Measure and Cohort Service available via REST API"

ENV WLP_HOME=/opt/ibm/wlp \
    SERVER_NAME=cohortServer \
    ALVEARIE_HOME=/opt/alvearie \
    COHORT_DIST_SOLUTION=/app/cohortSolutionDistribution \
    JAVA_HOME=/opt/ibm/java
ENV COHORT_ENGINE_HOME=$ALVEARIE_HOME/cohortEngine

# create server instance
RUN $WLP_HOME/bin/server create $SERVER_NAME && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/resources/security && \
    mkdir -p $WLP_HOME/usr/servers/$SERVER_NAME/properties

USER root
# Update image to pick up latest security updates
# Make dir for test resources
# Update symlnk used by Liberty to new server.  Need root.
RUN microdnf update -y && rm -rf /var/cache/yum && \
    microdnf install -y --nodocs vim openssl && \
    microdnf clean all && \
    ln -sfn $WLP_HOME/usr/servers/$SERVER_NAME /config

#Copy in war files, config files, etc. to final image
COPY --from=builder --chown=1001:1001 $COHORT_DIST_SOLUTION/solution/webapps/*.war /config/apps/
COPY --from=builder --chown=1001:1001 $COHORT_DIST_SOLUTION/solution/bin/server.xml /config/
COPY --from=builder --chown=1001:1001 $COHORT_DIST_SOLUTION/solution/bin/jvm.options /config/
# copy the cohort engine uber jar (aka shaded jar)
COPY --from=builder --chown=1001:1001 $COHORT_DIST_SOLUTION/solution/jars/*.jar $COHORT_ENGINE_HOME/

# Setup path
ENV PATH="$JAVA_HOME/jre/bin:${PATH}"

# Copy our startup script into the installed Liberty bin
COPY --from=builder $COHORT_DIST_SOLUTION/solution/bin/*.sh $WLP_HOME/bin/

# Grant write access to apps folder and startup script
RUN chown -R --from=root whuser $WLP_HOME && \
    chmod -R u+rwx $WLP_HOME && \
    chown -R --from=root whuser $COHORT_ENGINE_HOME && \
    chmod -R u+rwx $COHORT_ENGINE_HOME

# install any missing features required by server config
RUN $WLP_HOME/bin/installUtility install --acceptLicense $SERVER_NAME

#DEBUG
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/" ]
#RUN ["/bin/bash", "-c", "ls -al $WLP_HOME/usr/servers/$SERVER_NAME/*" ]

USER whuser

# Expose the servers HTTP and HTTPS ports.  NOTE:  must match with hardcoded testcase stage scripts, Helm charts (values.yaml), server.xml
EXPOSE 9080 9443

ENTRYPOINT $WLP_HOME/bin/entrypoint.sh