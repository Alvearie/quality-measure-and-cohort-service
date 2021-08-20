#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
# The kubernetes secret used to access the Cloud Object Store bucket
# This secret is used during development and is provided as an example
# of the format required by the cohorting engine spark code. The secret
# is referenced on the call to spark-submit
resource "kubernetes_secret" "spark_cos_secret" {
  metadata {
    name = var.spark_cos_secret_name
    namespace = var.spark_cos_secret_namespace
  }

  data = {
    AWS_SECRET_KEY = var.spark_cos_aws_secret_key
    AWS_ACCESS_KEY = var.spark_cos_aws_access_key
    AWS_ENDPOINT = var.spark_cos_aws_endpoint
    AWS_LOCATION = var.spark_cos_aws_location
  }

}