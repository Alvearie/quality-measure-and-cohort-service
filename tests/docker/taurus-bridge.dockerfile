FROM blazemeter/taurus

RUN pip3 install numpy

RUN pip3 install python-javabridge

ARG KUBE_VERSION=1.16.1
ENV API_KEY=“set-me”

RUN  curl -fsSL https://clis.cloud.ibm.com/install/linux | bash && \
    ibmcloud plugin install container-service -f && \
    curl -sL "https://storage.googleapis.com/kubernetes-release/release/v$KUBE_VERSION/bin/linux/amd64/kubectl" > /usr/local/bin/kubectl && \
    chmod 755 /usr/local/bin/kubectl && \
    kubectl version --client

COPY cloudInit.sh /

#RUN /cloudInit.sh pziZ4lJxZY_pQCgByMPWISjxy5AN_9_L875S-mtGjqX6 CDT_CommOps_Quick_Cluster_01 CDT_CommOps_Quick_Cluster_01_RG cdt-commops-quickteam01-ns-01

ENTRYPOINT ["sh", "-c", "/cloudInit.sh ${API_KEY} CDT_CommOps_Quick_Cluster_01 CDT_CommOps_Quick_Cluster_01_RG cdt-commops-quickteam01-ns-01; bzt -l /tmp/artifacts/bzt.log \"$@\"", "ignored"]