#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
#!/bin/sh

# Must match SERVER_NAME value in Dockerfile
LIBERTY_SERVER_NAME=cohortServer
export LIBERTY_SERVER_NAME

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "DIR is '$DIR'"

.${DIR}/setupLiberty.sh
.${DIR}/runServer.sh