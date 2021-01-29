The CQL engine needs to trust the SSL certificate for the FHIR server. In IBM Cloud Development/Test (CDT) environments, these are self-signed certificates. Users need to download the server's certificate and import it into a trust store that will be used by the JVM. There are a bunch of ways to do this, including through the use of custom SSLContexts on the HAPI server or just importing directly into the JVM's truststore, but right now we assume a custom truststore will be specified using JVM arguments.

0) Optional: If you don't have network line-of-sight to the FHIR server running in CDT, use Kubernetes port-forward to make a connection

`kubectl -n <namespace> port-forward service/fhir-internal 9443:9443`

1) Download the certificate from the server (assumes localhost with Kubernetes port mapped to the CDT server of interest)

`echo | openssl s_client -connect localhost:9443 -showcerts 2>/dev/null | openssl x509 > fhir-server.cer`

2) Import the certificate into a new Java keystore

`keytool -noprompt -importcert -keystore trustStore.pkcs12 -storepass change-password -alias fhir-server -storetype pkcs12 < fhir-server.cer`

3) Specify the location and password of the keystore in the JVM arguments

`java -Djavax.net.ssl.trustStore=/path/to/trustStore.pkcs12 -Djavax.net.ssl.trustStorePassword=change-password -Djavax.net.ssl.trustStoreType=pkcs12 -jar target/cohort-engine*-shaded.jar ...`