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
    -Dspark.hadoop.fs.s3a.secret.key=$AWS_SECRET_KEY \
    -Dspark.history.fs.cleaner.enabled=$SPARK_HISTORY_FS_CLEANER_ENABLED \
    -Dspark.history.fs.cleaner.interval=$SPARK_HISTORY_FS_CLEANER_INTERVAL \ 
    -Dspark.history.fs.cleaner.maxAge=$SPARK_HISTORY_FS_CLEANER_MAXAGE \
    -Dspark.history.fs.cleaner.maxNum=$SPARK_HISTORY_FS_CLEANER_MAXNUM;"

/opt/spark/bin/spark-class org.apache.spark.deploy.history.HistoryServer;
