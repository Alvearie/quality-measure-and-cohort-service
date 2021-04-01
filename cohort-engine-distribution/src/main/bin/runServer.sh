#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
#!/bin/sh
# start server here, match name from DockerFile
SERVER_NAME=cohortServer

# tail the server log so it goes to stdout and thus into LogDNA
touch /logs/messages.log
tail -F /logs/messages.log &

# this command must execute synchonously, if we use server start $SERVER_NAME,
# then the container will exit when the command returns
/opt/ibm/wlp/bin/server run $SERVER_NAME