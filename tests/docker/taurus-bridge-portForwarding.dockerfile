FROM blazemeter/taurus
#Install the numpy package
RUN pip3 install numpy
# Not currently using javabridge for taurus-based test automation
# RUN pip3 install python-javabridge
ARG WORKDIR=/tmp
ARG APACHE_LOG4J2="apache-log4j-2.17.1-bin"
#Download and extract the apache log4j version 2.17.1 jar files
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
WORKDIR ${WORKDIR}
RUN set -x && \
    wget -nv -O ${WORKDIR}/${APACHE_LOG4J2}.tar.gz "https://dlcdn.apache.org/logging/log4j/2.17.1/${APACHE_LOG4J2}.tar.gz" && \
    tar -xzf ${WORKDIR}/${APACHE_LOG4J2}.tar.gz --strip-components=1 -C ${WORKDIR}
#Copy the four new log4j version 2.17.1 jar files from the extracted tar ball into the taurus container file system in the right directory.
RUN mv ${WORKDIR}/log4j-1.2-api-2.17.1.jar /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-1.2-api-2.17.1.jar
RUN mv ${WORKDIR}/log4j-api-2.17.1.jar /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-api-2.17.1.jar
RUN mv ${WORKDIR}/log4j-core-2.17.1.jar /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-core-2.17.1.jar
RUN mv ${WORKDIR}/log4j-slf4j-impl-2.17.1.jar /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-slf4j-impl-2.17.1.jar
#Remove the older version 2.16.0 log4j jar files from the built blazemeter/taurus source docker image
RUN rm /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-1.2-api-2.16.0.jar
RUN rm /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-api-2.16.0.jar
RUN rm /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-core-2.16.0.jar
RUN rm /root/.bzt/jmeter-taurus/5.4.2/lib/log4j-slf4j-impl-2.16.0.jar
#Rename the log4j-*-2.16.0.jar files under licenses directory to log4j-*-2.17.1.jar instead.
RUN mv /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-1.2-api-2.16.0.jar /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-1.2-api-2.17.1.jar
RUN mv /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-api-2.16.0.jar /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-api-2.17.1.jar
RUN mv /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-core-2.16.0.jar /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-core-2.17.1.jar
RUN mv /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-slf4j-impl-2.16.0.jar /root/.bzt/jmeter-taurus/5.4.2/licenses/log4j-slfj-impl-2.17.1.jar
ARG KUBE_VERSION=1.21.4
ENV API_KEY="set-me"
ENV CLUSTER_NAME="set-me"
ENV REGION="set-me"
ENV CLUSTER_RESOURCE_GROUP="set-me"
ENV CLUSTER_NAMESPACE="set-me"
RUN  curl -fsSL https://clis.cloud.ibm.com/install/linux | bash && \
    ibmcloud plugin install container-service -f && \
    curl -sL "https://storage.googleapis.com/kubernetes-release/release/v$KUBE_VERSION/bin/linux/amd64/kubectl" > /usr/local/bin/kubectl && \
    chmod 755 /usr/local/bin/kubectl && \
    kubectl version --client
COPY cloudInit.sh /
ENTRYPOINT ["sh", "-c", "/cloudInit.sh ${CLOUD_API_KEY} ${REGION} ${CLUSTER_RESOURCE_GROUP} ${CLUSTER_NAME} ${CLUSTER_NAMESPACE}; bzt -l /tmp/artifacts/bzt.log \"$@\"", "ignored"]