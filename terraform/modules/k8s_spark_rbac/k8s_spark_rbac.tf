#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
## -----------------------------------------------------------------------------------------
# Service Account
## -----------------------------------------------------------------------------------------
resource "kubernetes_service_account" "spark" {
  metadata {
    name = "spark"
    namespace = var.k8s_spark_rbac_namespace_name
  }
}

## -----------------------------------------------------------------------------------------
# Role
## -----------------------------------------------------------------------------------------
resource "kubernetes_role" "spark" {
  metadata {
    name = "spark-role"
    namespace = var.k8s_spark_rbac_namespace_name  }

  rule {
    api_groups     = [""]
    resources      = ["pods"]
    verbs          = ["get", "list", "watch", "create", "update", "patch", "delete", "deletecollection"]
  }

  rule {
    api_groups     = [""]
    resources      = ["services"]
    verbs          = ["get", "list", "watch", "create", "update", "patch", "delete", "deletecollection"]
  }

  rule {
    api_groups     = [""]
    resources      = ["configmaps"]
    verbs          = ["get", "list", "watch", "create", "update", "patch", "delete", "deletecollection"]
  }
}

## -----------------------------------------------------------------------------------------
# Role Binding
## -----------------------------------------------------------------------------------------
resource "kubernetes_role_binding" "spark" {
  metadata {
    name      = "spark-role-binding"
    namespace = var.k8s_spark_rbac_namespace_name
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "Role"
    name      = "spark-role" 
  }
  subject {
    kind      = "ServiceAccount"
    name      = "spark" 
    namespace = var.k8s_spark_rbac_namespace_name
  }
}