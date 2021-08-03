#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
variable "service_account_secret_name" {
  description = "Secret name used to pull spark images from container registry namespace"
  type = string
  default = "spark-docker-cfg"
}
variable "service_account_dockerconfigjson_namespace" {
  description = "Secret namespace to create to pull spark images from container registry namespace"
  type = string
}
variable "service_account_dockerconfigjson" {
  description = "Dockerconfigjson value used to pull spark images from container registry namespace"
  type = string
}