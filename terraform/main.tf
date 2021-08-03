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
<<<<<<< Upstream, based on origin/main
# Kubernetes signed certificate creation for REST API server
##############################################################################
=======

#module "app-id" {
#  source		= "./modules/app-id"
#  resource_group_id	= data.ibm_resource_group.resource_group_cloudsvc.id
#  appid_name		= var.appid_name
#  appid_plan		= var.appid_plan
#  appid_location	= var.appid_location
#}

#module "keyprotect" {
#  source		= "./modules/keyprotect"
#  resource_group_id	= data.ibm_resource_group.resource_group_cloudsvc.id
#  kp_name		= var.kp_name
#  kp_plan		= var.kp_plan
#  kp_location		= var.kp_location
#}

#module "certificate-manager" {
#  source                = "./modules/certificate-manager"
#  resource_group_id     = data.ibm_resource_group.resource_group_cloudsvc.id
#  cm_name               = var.cm_name
#  cm_location		= var.cm_location
#}

#module "internet-services" {
#  source                = "./modules/internet-services"
#  resource_group_id     = data.ibm_resource_group.resource_group_cloudsvc.id
#  internet_svcs_name    = var.internet_svcs_name
#  internet_svcs_plan    = var.internet_svcs_plan
#  tag_list              = var.tag_list
#}

#module "cloud-object-storage" {
#  source                		= "./modules/cloud-object-storage"
#  resource_group_id     		= data.ibm_resource_group.resource_group_cloudsvc.id
#  ibm_region            		= var.ibm_region
#  cos_plan				= var.cos_plan
#  cos_instance_one_name			= var.cos_instance_one_name
#  cos_instance_two_name 		= var.cos_instance_two_name
#  kp_name               		= var.kp_name
#  scia_at_us_east_id			= data.ibm_resource_instance.scia_at_us_east.id
#  ibm_region_cos			= var.ibm_region_cos
#  cos_bucket_name_landing_dev		= var.cos_bucket_name_landing_dev
#  cos_bucket_name_publish_dev		= var.cos_bucket_name_publish_dev
#  cos_bucket_name_logs_dev		= var.cos_bucket_name_logs_dev
#  cos_bucket_name_tmp_dev		= var.cos_bucket_name_tmp_dev
#  cos_bucket_name_landing_test		= var.cos_bucket_name_landing_test
#  cos_bucket_name_publish_test		= var.cos_bucket_name_publish_test
#  cos_bucket_name_logs_test		= var.cos_bucket_name_logs_test
#  cos_bucket_name_tmp_test		= var.cos_bucket_name_tmp_test
#  cos_bucket_name_nifi			= var.cos_bucket_name_nifi
#  cos_bucket_name_prometheus		= var.cos_bucket_name_prometheus
#  cos_bucket_name_uap			= var.cos_bucket_name_uap
#  cos_bucket_name_publish_awb		= var.cos_bucket_name_publish_awb
#  cos_bucket_name_jupyterlab		= var.cos_bucket_name_jupyterlab
#  ibm_resource_key_dev_name		= var.ibm_resource_key_dev_name
#  ibm_resource_key_test_name		= var.ibm_resource_key_test_name
#  ibm_resource_key_nifi_name_writer	= var.ibm_resource_key_nifi_name_writer
#  ibm_resource_key_nifi_name_manager	= var.ibm_resource_key_nifi_name_manager
#  depends_on        			= [module.keyprotect]
#}

#module "k8s_namespaces" {
#  source                        = "./modules/k8s_namespaces"
#  configure_namespace		= var.configure_namespace
#}

#module "k8s_secrets" {
#  source                        	= "./modules/k8s_secrets"
#  msn_ls_ns_01_name			= data.kubernetes_namespace.msn_ls_ns_01.0.metadata[0].name
#  test_access_key_id			= module.cloud-object-storage.test_access_key_id
#  test_secret_access_key		= module.cloud-object-storage.test_secret_access_key
#  dev_access_key_id			= module.cloud-object-storage.dev_access_key_id
#  dev_secret_access_key			= module.cloud-object-storage.dev_secret_access_key
#  nifi_access_key_id			= module.cloud-object-storage.nifi_access_key_id
#  nifi_secret_access_key		= module.cloud-object-storage.nifi_secret_access_key
#  nifi_secret_apikey			= module.cloud-object-storage.nifi_secret_apikey
#  cos_access_nophi_test_secret_name	= var.cos_access_nophi_test_secret_name
#  cos_access_phi_dev_secret_name	= var.cos_access_phi_dev_secret_name
#  cos_write_access_nifi_name		= var.cos_write_access_nifi_name
#  organization				= var.organization
#  deid_service_name			= var.deid_service_name
#  resource_group_id     		= data.ibm_resource_group.resource_group_cloudsvc.id
#  cm_name				= var.cm_name
#  depends_on                    	= [module.cloud-object-storage,module.certificate-manager]
#}

module "k8s_secrets" {
  source                                     = "./modules/k8s_secrets"
  service_account_dockerconfigjson           = base64decode(var.service_account_dockerconfigjson)
  service_account_dockerconfigjson_namespace = data.kubernetes_namespace.spark_k8s_namespace.0.metadata[0].name
}

module "k8s_spark_rbac" {
  source                                = "./modules/k8s_spark_rbac"
#  msn_ls_ns_01_name                     = data.kubernetes_namespace.msn_ls_ns_01.0.metadata[0].name
  k8s_spark_rbac_namespace_name         = data.kubernetes_namespace.spark_k8s_namespace.0.metadata[0].name
}

#module "k8s_pvc" {
#  source				= "./modules/k8s_pvc"
#  msn_ls_ns_01_name			= data.kubernetes_namespace.msn_ls_ns_01.0.metadata[0].name
#  cos_bucket_name_tmp_dev		= var.cos_bucket_name_tmp_dev
#  cos_bucket_name_tmp_test		= var.cos_bucket_name_tmp_test
#  ibm_region				= var.ibm_region
#  pvc_spark_tmp_dev_name		= var.pvc_spark_tmp_dev_name
#  pvc_spark_tmp_test_name		= var.pvc_spark_tmp_test_name
#  cos_access_phi_dev_secret_name	= var.cos_access_phi_dev_secret_name
#  cos_access_nophi_test_secret_name	= var.cos_access_nophi_test_secret_name
#  pvc_spark_tmp_dev_storage_requests	= var.pvc_spark_tmp_dev_storage_requests
#  pvc_spark_tmp_test_storage_requests	= var.pvc_spark_tmp_test_storage_requests
#  depends_on				= [module.cloud-object-storage,module.k8s_secrets]
#}

# Uncomment if you want terraform to create a separate container registry namespace
# to contain your spark images
module "cr_namespace" {
  source                    = "./modules/cr_namespace"
  resource_group_id         = data.ibm_resource_group.resource_group_cloudsvc.id
  cr_namespace_name         = var.spark_cr_namespace_name
  cr_ns_region              = var.cr_ns_region
}
>>>>>>> fdb1d0b add k8s secret for imagePullSecret

module "tls_cert" {
  source                    = "./modules/tls_cert"
  namespace                 = var.tsl_cert_namespace
  service_names             = var.tsl_cert_service_names
  organization              = var.tsl_cert_organization
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
  spark_cos_aws_secret_key                   = var.spark_cos_aws_secret_key
  spark_cos_aws_access_key                   = var.spark_cos_aws_access_key
  spark_cos_aws_endpoint                     = var.spark_cos_aws_endpoint
  spark_cos_aws_location                     = var.spark_cos_aws_location
}

