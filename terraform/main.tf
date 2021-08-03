#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

## -----------------------------------------------------------------------------------------
# Define already exising Resource Group resources
## -----------------------------------------------------------------------------------------

data "ibm_resource_group" "resource_group_cloudsvc" {
  name = var.vpc_rg_cloudsvc
}

#data "ibm_resource_group" "resource_group_ops" {
#  name = var.vpc_rg_ops
#}

data "ibm_resource_group" "resource_group_kube" {
  name = var.vpc_rg_kube
}

#data "ibm_resource_group" "resource_group_default" {
#  name = var.vpc_rg_default
#}

#data "ibm_resource_group" "resource_group_scia" {
#  name = var.vpc_rg_scia
#}

data "ibm_container_cluster_config" "cluster" {
  cluster_name_id = var.cluster_name
  resource_group_id = data.ibm_resource_group.resource_group_kube.id
  admin	= true
}

## -----------------------------------------------------------------------------------------
# Define already exising Activity Tracker resources
## -----------------------------------------------------------------------------------------

#data "ibm_resource_instance" "scia_at_au_syd" {
#  name                  = var.vpc_scia_at_au_syd_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}

#data "ibm_resource_instance" "scia_at_eu_de" {
#  name                  = var.vpc_scia_at_eu_de_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}

#data "ibm_resource_instance" "scia_at_eu_gb" {
#  name                  = var.vpc_scia_at_eu_gb_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}

#data "ibm_resource_instance" "scia_at_jp_tok" {
#  name                  = var.vpc_scia_at_jp_tok_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}

#data "ibm_resource_instance" "scia_at_us_east" {
#  name                  = var.vpc_scia_at_us_east_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}

#data "ibm_resource_instance" "scia_at_us_south" {
#  name                  = var.vpc_scia_at_us_south_name
#  resource_group_id     = data.ibm_resource_group.resource_group_scia.id
#}


## -----------------------------------------------------------------------------------------
# Define already exising Namespaces - created by Toolchain
## -----------------------------------------------------------------------------------------
#data "kubernetes_namespace" "msn_ls_ns_01" {
data "kubernetes_namespace" "spark_k8s_namespace" {
  count = var.kubernetes_namespace != "" ? 1 : 0

  metadata {
    name = var.kubernetes_namespace
  }
}

##############################################################################
# Kubernetes Provider
##############################################################################

provider kubernetes {
  host                   = data.ibm_container_cluster_config.cluster.host
  client_certificate     = data.ibm_container_cluster_config.cluster.admin_certificate
  client_key             = data.ibm_container_cluster_config.cluster.admin_key
  cluster_ca_certificate = data.ibm_container_cluster_config.cluster.ca_certificate
}

##############################################################################

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

module "tls_cert" {
  source                    = "./modules/tls_cert"
  namespace                 = var.tsl_cert_namespace
  service_names             = var.tsl_cert_service_names
  organization              = var.tsl_cert_organization
}








## START ORIG

# key used to generate Certificate Request
#resource "tls_private_key" "private_key" {
#  algorithm   = "ECDSA"
#  ecdsa_curve = "P384"
#}

#locals {
#  dns_names = concat([for service in var.service_names : [service, "${service}.${var.namespace}", "${service}.${var.namespace}.svc"]]...)
#}

# Construct the CSR
#resource "tls_cert_request" "cert_request" {
#  for_each = toset(var.service_names)

#  key_algorithm   = "ECDSA"
#  private_key_pem = tls_private_key.private_key.private_key_pem
#  dns_names       = [each.key, "${each.key}.${var.namespace}", "${each.key}.${var.namespace}.svc"]

#  subject {
#    common_name  = each.key
#    organization = var.organization
#  }
#}

# Issue the certificate signing request
#resource "kubernetes_certificate_signing_request" "csr" {
#  for_each = toset(var.service_names)

#  metadata {
#    generate_name = "${each.key}-csr"
#  }

#  spec {
#    usages  = ["client auth", "server auth"]
#    request = tls_cert_request.cert_request[each.key].cert_request_pem
#  }
#  auto_approve = true
#}

#data "kubernetes_namespace" "namespace" {
#  metadata {
#    name = var.namespace
#  }
#}

# Get the signed certificate from the request and save it in the secret
#resource "kubernetes_secret" "secret" {
#  for_each = toset(var.service_names)

#  metadata {
#    name      = "${each.key}-tls"
#    namespace = data.kubernetes_namespace.namespace.metadata[0].name
#  }
#  data = {
#    "tls.crt" = kubernetes_certificate_signing_request.csr[each.key].certificate
#    "tls.key" = tls_private_key.private_key.private_key_pem
#  }
#  type = "kubernetes.io/tls"
#}
