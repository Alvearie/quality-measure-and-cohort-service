#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
variable "image_pull_secret_name" {
  description = "Secret name used to pull spark images from container registry namespace"
  type = string
  default = "spark-image-pull-secret"
}
variable "image_pull_secret_namespace" {
  description = "Secret namespace to create to pull spark images from container registry namespace"
  type = string
}
variable "image_pull_secret_dockerconfigjson" {
  description = "Dockerconfigjson value used to pull spark images from container registry namespace"
  type = string
}