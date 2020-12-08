FROM openliberty/open-liberty:20.0.0.12-full-java8-openj9-ubi

# Update the image to avoid vulnerabilities
USER root
RUN yum update -y --nobest && \
	yum install -y unzip && \
	yum clean all -y
USER 1001

# Add Liberty server configuration including all necessary features
COPY --chown=1001:0  cohort-engine-api-web/src/main/resources/server.xml /config/

# Add app
COPY --chown=1001:0  cohort-engine-api-web/target/cohort-engine-api-web-*.war /config/apps/

# This script will add the requested server configurations, apply any interim fixes and populate caches to optimize runtime
RUN configure.sh