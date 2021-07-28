#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
variable "k8s_spark_rbac_namespace_name" {
  type = string
}

variable "spark_service_account_name" {
  description = "Name of the service account to be used for spark on k8s"
  type = string
  default = "spark"
}

variable "spark_kubernetes_role_name" {
  description = "Name of the role to be used for spark on k8s"
  type = string
  default = "spark-role"
}

variable "spark_kubernetes_role_binding_name" {
  description = "Name of the role binding to be used for spark on k8s"
  type = string
  default = "spark-role-binding"
}