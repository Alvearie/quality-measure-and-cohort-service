#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
variable "spark_cos_secret_name" {
  description = "Name of Cloud Object Store (cos) secret used by spark to access cos"
  type = string
}
variable "spark_cos_secret_namespace" {
  description = "Namespace containing cos secret used by spark to access cos"
  type = string
}
variable "spark_cos_aws_secret_key" {
  description = "AWS secret key value for cos"
  type = string
}
variable "spark_cos_aws_access_key" {
  description = "AWS access key value for cos"
  type = string
}
variable "spark_cos_aws_endpoint" {
  description = "AWS endpoint value for cos"
  type = string
}
variable "spark_cos_aws_location" {
  description = "AWS location value for cos"
  type = string
}