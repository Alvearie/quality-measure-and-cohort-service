#!/bin/bash

echo | openssl s_client -connect fhir-internal.${FHIR_CLUSTER_NAMESPACE}.svc:9443 -showcerts 2>/dev/null | openssl x509 > fhir-server.cer

keytool -noprompt -importcert -keystore trustStore.pkcs12 -storepass ${TRUSTSTORE_PASSWORD} -alias fhir-server -storetype pkcs12 < fhir-server.cer

mv ./trustStore.pkcs12 /bzt-configs/${TRUSTSTORE}