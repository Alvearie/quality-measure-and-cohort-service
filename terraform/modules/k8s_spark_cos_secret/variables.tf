#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
variable "spark_cos_secret_name" {
  description = "Name of Cloud Object Store (COS) secret used by spark to access COS"
  type = string
}
variable "spark_cos_secret_namespace" {
  description = "Namespace containing COS secret used by spark to access COS"
  type = string
}
variable "spark_cos_resource_key_manager" {
  description = "IBM Resource Key name for Manager role in COS configuration bucket"
  type = string
}
variable "spark_cos_resource_key_writer" {
  description = "IBM Resource Key name for Writer role in COS configuration bucket"
  type = string
}
variable "config_volume_claim_name" {
  description = "Kubernetes PVC resource name for configuration data"
  type = string
}
variable "config_bucket_name" {
  description = "COS bucket for configuration storing configuration data"
  type = string
}