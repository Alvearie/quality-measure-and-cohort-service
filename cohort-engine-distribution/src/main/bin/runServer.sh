#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
#!/bin/sh
# start server here, match name from DockerFile
SERVER_NAME=cohortServer
/opt/ibm/wlp/bin/server start $SERVER_NAME
# TODO restart server to get SSL working until we preload certs/keys from secrets in cloud?
#/opt/ibm/wlp/bin/server stop $SERVER_NAME
#/opt/ibm/wlp/bin/server start $SERVER_NAME

# tail the server log so it goes to stdout and thus into LogDNA
tail -f /logs/messages.log &

# TODO remove before PROD: Keep alive in case server readiness/liveness fail and keep pod alive
while true
do
	echo "Keep alive in case server readiness/liveness fail and keep pod alive. sleeping..."
	sleep 600
done
# TODO Liberty server outputs console and service logs to somewhere?
# TODO Mount object store into file system for log files or other artifacts in deployment helm