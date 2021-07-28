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
<<<<<<< Upstream, based on origin/main
    name = var.spark_service_account_name
    namespace = var.k8s_spark_rbac_namespace_name
  }
  
  #created in k8s_secrets module
  image_pull_secret {
    name = var.k8s_spark_rbac_image_pull_secret_name
  }
}

## -----------------------------------------------------------------------------------------
# Role
## -----------------------------------------------------------------------------------------
resource "kubernetes_role" "spark" {
  metadata {
    name = var.spark_kubernetes_role_name
    namespace = var.k8s_spark_rbac_namespace_name  
  }

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
    name      = var.spark_kubernetes_role_binding_name
    namespace = var.k8s_spark_rbac_namespace_name
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "Role"
    name      = var.spark_kubernetes_role_name
  }
  subject {
    kind      = "ServiceAccount"
    name      = var.spark_service_account_name
=======
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
    namespace = var.k8s_spark_rbac_namespace_name  
  }

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
>>>>>>> d5bda4e refactor tls cert terraform into module and add rbac config
    namespace = var.k8s_spark_rbac_namespace_name
  }
}