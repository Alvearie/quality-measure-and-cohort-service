#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
resource "kubernetes_secret" "spark_dockerconfigjson" {
  metadata {
    name = var.image_pull_secret_name
    namespace = var.image_pull_secret_namespace
  }

  data = {
    ".dockerconfigjson" = var.image_pull_secret_dockerconfigjson
  }

  type = "kubernetes.io/dockerconfigjson"
}