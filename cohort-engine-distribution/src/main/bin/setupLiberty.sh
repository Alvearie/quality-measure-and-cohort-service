LIBERTY_INSTALL_ROOT=/opt/ibm
LIBERTY_SERVER_LOC=/wlp/usr/servers
#LIBERTY_SERVER_NAME=cohortServer
SERVER_XML_FILE=/config/server.xml
LIBERTY_TRUST_STORE_LOC=${LIBERTY_INSTALL_ROOT}/wlp/output/$LIBERTY_SERVER_NAME/resources/security
K8S_CERT_BUNDLE_LOC=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt

# encodePasswordForLiberty
# Return liberty encoded password
# Parm 1 - required, Liberty install root
#          Directory must exist and contain valid liberty install
# Parm 2 - required, Password to encode
# Parm 3 = Required, Variable name to return encoded password value in
encodePasswordForLiberty() {
  local liberty_install_root=$1
  local store_pwd=$2
  local encoded_pwd

  encoded_pwd=`$liberty_install_root/wlp/bin/securityUtility encode $store_pwd`
  checkForError $? "Failed to encode password"
  eval "$3=$encoded_pwd"
}

# replaceToken
# Replace the specified token with the specified
# value in the specified file
# Parm 1: required, File to replace token in
# Parm 2: required, Token
# Parm 3: required, Value
#
replaceToken() {
  local f=$1
  local t=$2
  local v=$3
  local rc

  sed -i "s!$t!$v!g" $f
  rc=$?

  return $rc
}

# Generate encoded/encrypted passwords and token
LIBERTY_STORE_PWD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 15 | head -n 1)
#echo "LIBERTY_STORE_PWD=$LIBERTY_STORE_PWD" >> /opt/alvearie/store_info
#chmod 640 $WORA_INSTALL_ROOT/config/store_info
LIBERTY_STORE_ENCODED_PWD=
encodePasswordForLiberty $LIBERTY_INSTALL_ROOT $LIBERTY_STORE_PWD LIBERTY_STORE_ENCODED_PWD
#export ENCODED_PWD_TOKEN=$LIBERTY_STORE_ENCODED_PWD
replaceToken $SERVER_XML_FILE ENCODED_PWD_TOKEN $LIBERTY_STORE_ENCODED_PWD

# create keystore using K8s CA signed certificate and key in mounted secret
# a comOpps ticket is needed to create this cert. It is mounted as a volume in the deployment yaml.
mkdir -p ${LIBERTY_TRUST_STORE_LOC}
keytool -import -v -trustcacerts -alias k8s-cluster-cert -file ${K8S_CERT_BUNDLE_LOC} -keystore ${LIBERTY_TRUST_STORE_LOC}/keystore.p12 -storepass ${LIBERTY_STORE_PWD} -noprompt
#openssl pkcs12 -export -inkey /secrets/tls/tls.key -in /secrets/tls/tls.crt -out ${LIBERTY_TRUST_STORE_LOC}/keystore.p12 -password pass:${LIBERTY_STORE_PWD}
mv -f ${LIBERTY_TRUST_STORE_LOC}/keystore.p12 ${LIBERTY_TRUST_STORE_LOC}/cohortCDTTrust.p12
chmod 755 ${LIBERTY_TRUST_STORE_LOC}/cohortCDTTrust.p12