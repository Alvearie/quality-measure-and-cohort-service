#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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

variable "k8s_spark_rbac_image_pull_secret_name" {
  description = "Name of the image pull secret to be used for spark on k8s"
  type = string
  default = "spark-image-pull-secret"
<<<<<<< Upstream, based on origin/main
=======
#variable "msn_ls_ns_01_name" {
=======
>>>>>>> bd5f0c1 change rbac parms to be variables instead of hardcoded
variable "k8s_spark_rbac_namespace_name" {
  type = string
<<<<<<< Upstream, based on origin/main
>>>>>>> d5bda4e refactor tls cert terraform into module and add rbac config
=======
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
>>>>>>> bd5f0c1 change rbac parms to be variables instead of hardcoded
=======
>>>>>>> fdb1d0b add k8s secret for imagePullSecret
}