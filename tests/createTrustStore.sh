#!/bin/bash

echo | openssl s_client -connect fhir-internal.dev.svc:9443 -showcerts 2>/dev/null | openssl x509 > fhir-server.cer

keytool -noprompt -importcert -keystore trustStore.pkcs12 -storepass TStore-Password -alias fhir-server -storetype pkcs12 < fhir-server.cer

mv ./trustStore.pkcs12 /bzt-configs/tests/src/main/resources/config
