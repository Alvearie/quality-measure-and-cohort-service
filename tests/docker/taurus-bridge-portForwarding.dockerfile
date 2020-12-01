FROM blazemeter/taurus

RUN pip3 install numpy

RUN pip3 install python-javabridge

ARG KUBE_VERSION=1.16.1
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