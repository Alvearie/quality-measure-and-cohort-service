#!/bin/bash

#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

export SPARK_HISTORY_OPTS="$SPARK_HISTORY_OPTS \
    -Dspark.history.fs.logDirectory=s3a://$AWS_BUCKET$AWS_LOG_PATH \
    -Dspark.hadoop.fs.s3a.endpoint=$AWS_ENDPOINT \
    -Dspark.hadoop.fs.s3a.access.key=$AWS_ACCESS_KEY \
    -Dspark.hadoop.fs.s3a.secret.key=$AWS_SECRET_KEY";

/opt/spark/bin/spark-class org.apache.spark.deploy.history.HistoryServer;
