#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

# The official ubi8 image from redhat's registry is one of the few accepted base images for Alvearie
ARG BASE_IMAGE=registry.access.redhat.com/ubi8
ARG BASE_IMAGE_VERSION=8.4-206.1626828523

ARG SPARK_VERSION=spark-3.1.2
ARG SPARK_DIST=bin-hadoop3.2
ARG SPARK_UID=185
ARG SPARK_WORK_DIR=/spark-work-dir
ARG SPARK_HOME=/opt/spark


#################
# spark-builder #
#################
FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} AS spark-builder

ARG SPARK_HOME
ARG SPARK_VERSION
ARG SPARK_DIST

# Download and extract Spark
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN curl "https://archive.apache.org/dist/spark/${SPARK_VERSION}/${SPARK_VERSION}-${SPARK_DIST}.tgz" | tar -xz
# Move the extracted spark dist to SPARK_HOME
RUN mv "${SPARK_VERSION}-${SPARK_DIST}" "${SPARK_HOME}"
# Move entrypoint.sh to SPARK_HOME
RUN mv "${SPARK_HOME}/kubernetes/dockerfiles/spark/entrypoint.sh" "${SPARK_HOME}"
# Move decom.sh to SPARK_HOME
RUN mv "${SPARK_HOME}/kubernetes/dockerfiles/spark/decom.sh" "${SPARK_HOME}"
# Remove the `tini` use from entrypoint.sh.  Tini is unavailable on Red Hat.
RUN sed -i 's_/usr/bin/tini -s --__g' "${SPARK_HOME}/entrypoint.sh"


##############
# spark-base #
##############
FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION} as spark-base

ARG SPARK_WORK_DIR
ENV SPARK_WORK_DIR=${SPARK_WORK_DIR}
ARG SPARK_UID
ARG SPARK_HOME
ENV SPARK_HOME=${SPARK_HOME}

# Become root for the Spark installation process
USER root
# Perform a full update of all packages to minimize vulnerabilities
RUN dnf update --nobest -y &&\
    # Install Java 11
    dnf install -y java-11-openjdk &&\
    # Clean dnf
    dnf clean all

# Copy spark code from builder stage and chown to root
COPY --from=spark-builder --chown=root ${SPARK_HOME} ${SPARK_HOME}

# Add the spark user, create SPARK_WORK_DIR, and set it as the user's home directory
RUN useradd -l -u "${SPARK_UID}" -d "${SPARK_WORK_DIR}" spark

# Configure the runtime for downstream images
WORKDIR ${SPARK_WORK_DIR}
USER spark
ENTRYPOINT [ "/opt/spark/entrypoint.sh" ]

#################
# spark-history #
#################
FROM spark-base as spark-history

# Labels - certain labels are required if you want to have
#          a Red Hat certified image (this is not a full set per se)
LABEL maintainer="IBM Quality Measure and Cohort Service Team" \
      description="Quality Measure and Cohort Service Spark History Server Image" \
      name="spark-history-server" \
      vendor="Alvearie Open Source by IBM" \
      version="2.1.0" \ 
      release="2" \
      summary="Quality Measure and Cohort Service Spark History Server Image" \
      description="Quality Measure and Cohort Service Spark History Server Image"

# Become the root user
USER root

# Add the hadoop-aws jar and its aws dependency jar.
# hadoop-aws:3.2.0 was chosen as it lines up with the spark version in our base image.
# aws-java-sdk-bundle:1.11.375 was chosen as it's the primary dependency of hadoop-aws:3.2.0.
ADD https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.2.0/hadoop-aws-3.2.0.jar $SPARK_HOME/jars
ADD https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.11.375/aws-java-sdk-bundle-1.11.375.jar $SPARK_HOME/jars
# Ensure all Spark jars are readable by all users
RUN chmod +r "$SPARK_HOME"/jars/* &&\
    # Ensure all Spark jars are owned by root
    chown root:root "$SPARK_HOME"/jars/*

# Replace the existing entrypoint script with the history server one
COPY --chown=root:root entrypoint.sh $SPARK_HOME
# Make the entrypoint script executable by all users
RUN chmod +x "$SPARK_HOME/entrypoint.sh"

# Change back to the spark user
USER spark

