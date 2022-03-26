#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
# This terraform script can be used to create various cloud resources that may
# or may not be required depending on the cluster the cohorting engine is being
# deployed into. At a minimum, the cohorting engine requires:
# - Kubernetes signed tls certificates for use by the Liberty REST api server
# (optionally provisioned by these scripts)
# - A container registry service (not provisioned by these terraform scripts)
# with an associated namespace (optionally provisioned by these scripts), 
# - A Cloud Object Store instance (not provisioned by these scripts),
# - A COS bucket (not provisioned by these scripts) and associated COS secret
# to access the scripts (optionally provisioned by these scripts),
# - A service account with a role, rolebinding, and an imagePullSecret with 
# access to the container registry (optionally provisioned by these scripts)
#
# In some instances, the target cluster may already have some of these resources
# configured and in other cases, it may not. Therefore, you may need to edit
# the following terraform script to comment/uncomment the resources below
# that are needed for your environment.
#
# An override.tfvars file is required to override variable values for a particular
# deployment and is stored in a configuration repo if using IBM toolchains
# see https://pages.github.ibm.com/whc-toolchain/whc-commons/3.4.3/ready/terraform-integration/
##############################################################################

## -----------------------------------------------------------------------------------------
# Define references to already existing Resource Group resources
## -----------------------------------------------------------------------------------------

data "ibm_resource_group" "resource_group_cloudsvc" {
  name = var.vpc_rg_cloudsvc
}

data "ibm_resource_group" "resource_group_kube" {
  name = var.vpc_rg_kube
}

data "ibm_container_cluster_config" "cluster" {
  cluster_name_id = var.cluster_name
  resource_group_id = data.ibm_resource_group.resource_group_kube.id
  admin	= true
}

## -----------------------------------------------------------------------------------------
# Define reference to already existing namespaces
## -----------------------------------------------------------------------------------------
data "kubernetes_namespace" "spark_k8s_namespace" {
  count = var.spark_kubernetes_namespace != "" ? 1 : 0

  metadata {
    name = var.spark_kubernetes_namespace
  }
}

##############################################################################
# Kubernetes Provider required to provision k8s resources
##############################################################################

provider kubernetes {
  host                   = data.ibm_container_cluster_config.cluster.host
  client_certificate     = data.ibm_container_cluster_config.cluster.admin_certificate
  client_key             = data.ibm_container_cluster_config.cluster.admin_key
  cluster_ca_certificate = data.ibm_container_cluster_config.cluster.ca_certificate
}

##############################################################################
# Kubernetes signed certificate creation for REST API server
##############################################################################

module "tls_cert" {
  source                    = "./modules/tls_cert"
  namespace                 = var.tls_cert_namespace
  service_names             = var.tls_cert_service_names
  organization              = var.tls_cert_organization
}

##############################################################################
# Kubernetes imagePullSecret and service account creation required to allow
# spark to pull images from a containter registry and spin up pods as needed
##############################################################################

# an imagePullSecret is needed to allow the service account to pull images
# this must be created first so that the service account can reference it

module "k8s_spark_image_pull_secret" {
  source                                = "./modules/k8s_spark_image_pull_secret"
  image_pull_secret_name                = var.image_pull_secret_name
  image_pull_secret_dockerconfigjson    = base64decode(var.image_pull_secret_dockerconfigjson)
  image_pull_secret_namespace           = data.kubernetes_namespace.spark_k8s_namespace.0.metadata[0].name
}

# Creates a service account, role, and role binding which gives spark auth 
# to pull images and spin up spark pods
module "k8s_spark_rbac" {
  source                                = "./modules/k8s_spark_rbac"
  k8s_spark_rbac_image_pull_secret_name = var.image_pull_secret_name
  k8s_spark_rbac_namespace_name         = data.kubernetes_namespace.spark_k8s_namespace.0.metadata[0].name
}

##############################################################################
# Container registry namespace creation
##############################################################################
# Uncomment if you want terraform to create a separate container registry namespace
# to contain your spark images

#module "cr_namespace" {
#  source                    = "./modules/cr_namespace"
#  resource_group_id         = data.ibm_resource_group.resource_group_cloudsvc.id
#  cr_namespace_name         = var.spark_cr_namespace_name
#  cr_ns_region              = var.cr_ns_region
#}

##############################################################################
# Cloud Object Store Secret creation
##############################################################################
# Uncomment if you would like terraform to create a Cloud Object Store secret
# Most likely, actual users will want to manage COS buckets and their associated
# secrets outside of terraform since tenants with their own buckets could be added
# and removed in between deployments. We are providing the module below to illustrate
# how the secret should be formatted and to also easily facilitate development across 
# different namespaces
module "k8s_spark_cos_secret" {
  source                                     = "./modules/k8s_spark_cos_secret"
  spark_cos_secret_name                      = var.spark_cos_secret_name
  spark_cos_secret_namespace                 = data.kubernetes_namespace.spark_k8s_namespace.0.metadata[0].name
  spark_cos_resource_key_manager             = var.spark_cos_resource_key_manager
  spark_cos_resource_key_writer              = var.spark_cos_resource_key_writer
}