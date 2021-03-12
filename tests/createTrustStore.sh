#!/bin/bash

# check if FHIR_CLUSTER_NAMESPACE has been set in the toolchain
# properties and use it if it is there, assume fhir will be
# running in the same namespace
if [[ -z "${FHIR_CLUSTER_NAMESPACE}" ]]
then
  FHIR_CLUSTER_NAMESPACE_VAL=${CLUSTER_NAMESPACE}
else
  FHIR_CLUSTER_NAMESPACE_VAL=${FHIR_CLUSTER_NAMESPACE}
fi

echo FHIR_CLUSTER_NAMESPACE_VAL=${FHIR_CLUSTER_NAMESPACE_VAL}

echo | openssl s_client -connect fhir-internal.${FHIR_CLUSTER_NAMESPACE_VAL}.svc:9443 -showcerts 2>/dev/null | openssl x509 > fhir-server.cer

keytool -noprompt -importcert -keystore trustStore.pkcs12 -storepass ${TRUSTSTORE_PASSWORD} -alias fhir-server -storetype pkcs12 < fhir-server.cer

mv ./trustStore.pkcs12 /bzt-configs/${TRUSTSTORE}