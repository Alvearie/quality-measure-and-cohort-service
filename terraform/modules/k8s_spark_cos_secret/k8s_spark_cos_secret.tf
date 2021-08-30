#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

data "ibm_resource_key" "cos_manager" {
	name = var.spark_cos_resource_key_manager
}

data "ibm_resource_key" "cos_writer" {
	name = var.spark_cos_resource_key_writer
}

# The kubernetes secret used to access the Cloud Object Store bucket
# This secret is used during development and is provided as an example
# of the format required by the cohorting engine spark code. The secret
# is referenced on the call to spark-submit
resource "kubernetes_secret" "spark_cos_secret" {
  type = "ibm/ibmc-s3fs"
  metadata {
    name = var.spark_cos_secret_name
    namespace = var.spark_cos_secret_namespace
  }

  data = {
    secret-key = data.ibm_resource_key.cos_writer.credentials["cos_hmac_keys.secret_access_key"]
    access-key = data.ibm_resource_key.cos_writer.credentials["cos_hmac_keys.access_key_id"]
    res-conf-apikey = data.ibm_resource_key.cos_manager.credentials.apikey
  }
}

# The kubernetes persistence volume claim that is backed by COS and will
# be used to store configuration data.
resource "kubernetes_persistent_volume_claim" "pvc" {
  metadata {
    name = var.config_volume_claim_name
    namespace = var.spark_cos_secret_namespace
    annotations = {
      "ibm.io/auto-create-bucket" : "true"
      "ibm.io/auto-delete-bucket" : "false"
      "ibm.io/auto_cache" : "true"
      "ibm.io/bucket" : var.config_bucket_name
      "ibm.io/secret-name" : kubernetes_secret.spark_cos_secret.metadata[0].name
      "ibm.io/set-access-policy" : "true"
    }
  }
  spec {
    storage_class_name = "ibmc-s3fs-standard-regional"
    access_modes       = ["ReadWriteMany"]
    resources {
      requests = {
        storage = "200Mi"
      }
    }
  }
}
