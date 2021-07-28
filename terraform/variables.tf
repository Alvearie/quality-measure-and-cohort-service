#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#############################################################################
#
#  Resource Groups
#
#
###############################################################################
#variable vpc_rg_default {
#  type        = string
#  description = "Resource Group for cloud services"
#  default     = "Default"
#}

variable vpc_rg_cloudsvc {
  type        = string
  description = "Resource Group for cloud services"
  default     = "codev-dev1-wdc-cloudsvc"
}

#variable vpc_rg_ops {
#  type        = string
#  description = "Resource Group for Ops services"
#  default     = "msndev-dev1-wdc-ops"
#}

variable vpc_rg_kube {
  type        = string
  description = "Resource Group for IKS"
  default     = "codev-dev1-wdc-kube"
}

#variable vpc_rg_scia {
#  type        = string
#  description = "Resource Group for SCIA deployed services"
#  default     = "scia"
#}

variable resource_group {
  type        = string
  description = "Resource Group for cloud services.  Needed by Toolchain."
  default     = "codev-dev1-wdc-cloudsvc"
}

#############################################################################
#
#  Region
#
#
###############################################################################

variable "ibm_region" {
  type = string
  description	= "IBM Cloud region where all resources will be deployed"
  default	= "us-east"
}

#############################################################################
#
#  Already provisoned Activity Tracker Instances
#
#
###############################################################################

#variable vpc_scia_at_au_syd_name {
#  type        = string
#  description = "Name of Sydney Actvitiy Tracker Instance"
#  default     = "scia-at-au-syd"
#}

#variable vpc_scia_at_eu_de_name {
#  type        = string
#  description = "Name of Frankfurt Actvitiy Tracker Instance"
#  default     = "scia-at-eu-de"
#}

#variable vpc_scia_at_eu_gb_name {
#  type        = string
#  description = "Name of London Actvitiy Tracker Instance"
#  default     = "scia-at-eu-gb"
#}

#variable vpc_scia_at_jp_tok_name {
#  type        = string
#  description = "Name of Tokyo Actvitiy Tracker Instance"
#  default     = "scia-at-jp-tok"
#}

#variable vpc_scia_at_us_east_name {
#  type        = string
#  description = "Name of Washington Actvitiy Tracker Instance"
#  default     = "scia-at-us-east"
#}

#variable vpc_scia_at_us_south_name {
#  type        = string
#  description = "Name of Dallas Actvitiy Tracker Instance"
#  default     = "scia-at-us-south"
#}

#############################################################################
#
#   AppID
#
#
###############################################################################
#variable "appid_plan" {
#  description	= "The app id plan to provision"
#  type		= string
#  default	= "graduated-tier"
#}

#variable "appid_name" {
#  description	= "The name of the app id instance"
#  type		= string
#  default	= "terraform_starter_app_appid"
#}

#variable "appid_location" {
#  description   = "The location of the app id instance"
#  type          = string
#  default       = "us-east"
#}

#############################################################################
#
#   KeyProtect
#
#
###############################################################################
#variable "kp_plan" {
#  description	= "The key protect plan to provision"
#  type		= string
#  default	= "tiered-pricing"
#}

#variable "kp_name" {
#  description	= "The name of the keyprotect instance"
#  type		= string
#  default	= "terraform_starter_app_key_protect"
#}

#variable "kp_location" {
#  description   = "The location of the key protect instance"
#  type          = string
#  default       = "us-east"
#}

#############################################################################
#
#   Certificate Manager
#
#
###############################################################################
#variable "cm_name" {
#  description   = "The name of the certificate manager instance"
#  type          = string
#  default       = "terraform_starter_app_certificate_manager"
#}

#variable "cm_location" {
#  description   = "The location of the certificate manager instance"
#  type          = string
#  default       = "us-east"
#}
#############################################################################
#
#   Internet Services
#
#
###############################################################################
#variable "internet_svcs_name" {
#  description   = "The name of the internet services instance"
#  type          = string
#  default       = "terraform_starter_app_internet_services"
#}

#variable "internet_svcs_plan" {
#  description   = "The internet services plan to provision"
#  type          = string
#  default       = "enterprise-usage"
#}
#############################################################################
#
#   Cloud Object Storage
#
#
###############################################################################
#variable "cos_plan" {
#  description	= "The pricing plan for the Cloud Object Storage service. The command to see what options are available is: ibmcloud catalog service cloud-object-storage [lite | standard]"
#  type          = string
#  default 	= "lite"
#}

#variable "cos_instance_one_name" {
#  description   = "The name of the COS Instance - One"
#  type          = string
#  default       = "terraform_starter_app_cos_one"
#}

#variable "cos_instance_two_name" {
#  description   = "The name of the COS Instance - Two"
#  type          = string
#  default       = "terraform_starter_app_cos_two"
#}

#variable "ibm_region_cos" {
#  description	= "IBM Cloud region where all COS buckets will be deployed"
#  type		= string
#  default	= "us-east"
#}

#variable "cos_bucket_name_landing_dev" {
#  description   = "COS Bucket Name for Landing Zone - Dev"
#  type          = string
#  default       = "msn-vpc-landing-dev"
#}

#variable "cos_bucket_name_publish_dev" {
#  description   = "COS Bucket Name for Publish Zone - Dev"
#  type          = string
#  default       = "msn-vpc-publish-dev"
#}

#variable "cos_bucket_name_logs_dev" {
#  description   = "COS Bucket Name for Logs Zone - Dev"
#  type          = string
#  default       = "msn-vpc-logs-dev"
#}

#variable "cos_bucket_name_tmp_dev" {
#  description   = "COS Bucket Name for Spark Tmp Zone - Dev"
#  type          = string
#  default       = "msn-vpc-tmp-dev"
#}

#variable "cos_bucket_name_landing_test" {
#  description   = "COS Bucket Name for Landing Zone - Test"
#  type          = string
#  default       = "msn-vpc-landing-test"
#}

#variable "cos_bucket_name_publish_test" {
#  description   = "COS Bucket Name for Publish Zone - Test"
#  type          = string
#  default       = "msn-vpc-publish-test"
#}

#variable "cos_bucket_name_logs_test" {
#  description   = "COS Bucket Name for Logs Zone - Test"
#  type          = string
#  default       = "msn-vpc-logs-test"
#}

#variable "cos_bucket_name_tmp_test" {
#  description   = "COS Bucket Name for Spark Tmp Zone - Test"
#  type          = string
#  default       = "msn-vpc-tmp-test"
#}

#variable "cos_bucket_name_nifi" {
#  description   = "COS Bucket Name for NiFi"
#  type          = string
#  default       = "msn-vpc-nifi"
#}

#variable "cos_bucket_name_prometheus" {
#  description   = "COS Bucket Name for Prometheus"
#  type          = string
#  default       = "msn-vpc-prometheus"
#}

#variable "cos_bucket_name_uap" {
#  description   = "COS Bucket Name for UAP Reports"
#  type          = string
#  default       = "msn-vpc-uap"
#}

#variable "cos_bucket_name_publish_awb" {
#  description   = "COS Bucket Name for Publish Zone - AWB"
#  type          = string
#  default       = "msn-vpc-publish-awb"
#}

#variable "cos_bucket_name_jupyterlab" {
#  description   = "COS Bucket Name for Jupyter Lab"
#  type          = string
#  default       = "msn-vpc-jupyterlab"
#}

#variable ibm_resource_key_dev_name {
#  description   = "Resource Key Name for Dev"
#  type          = string
#  default       = "wh-msn-phi-dev"
#}

#variable ibm_resource_key_test_name {
#  description   = "Resource Key Name for Test"
#  type          = string
#  default       = "wh-msn-nophi-test"
#}

#variable ibm_resource_key_nifi_name_writer {
#  description   = "Resource Key Name for NiFi Writer"
#  type          = string
#  default       = "wh-msn-nifi-writer"
#}

#variable ibm_resource_key_nifi_name_manager {
#  description   = "Resource Key Name for NiFi Manager"
#  type          = string
#  default       = "wh-msn-nifi-manager"
#}
#############################################################################
#
#   Kubernetes
#
#
###############################################################################

variable cluster_name {
  type        = string
  description = "Name of the Cluster deployed by WH Automation"
  default     = "cohort-dev-1"
}

#############################################################################
#
#   Kubernetes Namespaces
#
#
###############################################################################

variable "configure_namespace" {
  type = list
  description = "Configures new namespace"
#  default     = ["monitoring","msn-observe"]
  default     = ["cicdtest"]
}

## -----------------------------------------------------------------------------------------
# Define already existing Namespaces
## -----------------------------------------------------------------------------------------

#variable kubernetes_namespace {
#  type        = string
# description = "Name of the namespace for MSN K8s Applications"
#  default     = "msn-dev-ls-ns-01"
#}

variable kubernetes_namespace {
  type        = string
  description = "Name of the namespace for K8s Applications"
  default     = "cicdtest"
}

## -----------------------------------------------------------------------------------------
# K8s secret
## -----------------------------------------------------------------------------------------
#variable "cos_access_nophi_test_secret_name" {
#  type		= string
#  description 	= "Name of the K8s secret for accessing test buckets"
#  default     	= "cos-access-nophi-test"
#}

#variable "cos_access_phi_dev_secret_name" {
#  type          = string
#  description 	= "Name of the K8s secret for accessing dev buckets"
#  default     	= "cos-access-phi-dev"
#}

#variable "cos_write_access_nifi_name" {
#  type          = string
#  description   = "Name of the K8s secret for accessing NiFi buckets"
#  default       = "cos-write-access-nifi"
#}

## -----------------------------------------------------------------------------------------
# K8s pvc
## -----------------------------------------------------------------------------------------
#variable "pvc_spark_tmp_dev_name" {
#  type          = string
#  description   = "Name of the K8s PVC for spark tmp bucket - dev"
#  default       = "spark-tmp-dev"
#}

#variable "pvc_spark_tmp_test_name" {
#  type          = string
#  description   = "Name of the K8s PVC for spark tmp bucket - test"
#  default       = "spark-tmp-test"
#}

#variable "pvc_spark_tmp_dev_storage_requests" {
#  type          = string
#  description   = "Storage Requests for PVC  - Spark Temp - Dev"
#  default       = "1Gi"
#}

#variable "pvc_spark_tmp_test_storage_requests" {
#  type          = string
#  description   = "Storage Requests for PVC  - Spark Temp - Test"
#  default       = "1Gi"
#}

## -----------------------------------------------------------------------------------------
# Container Registry Namespace Used to Hold Spark Images
## -----------------------------------------------------------------------------------------

variable "cr_namespace_name" {
  type          = string
  description   = "Name of the container registry namespace"
  default       = "vpc-dev-cohort-rns"
}

variable "cr_ns_region" {
  type          = string
  description   = "Region for the container registry namespace.  NB:  us-east not available"
  default       = "us-south"
}

## -----------------------------------------------------------------------------------------
# tags
## -----------------------------------------------------------------------------------------
variable "tag_list" {
  description	= "Tag list which could be added to the resource"
  type		= list(string)
  default	= ["tf_managed"]
}

## -----------------------------------------------------------------------------------------
# tls-cert
## -----------------------------------------------------------------------------------------

#variable "deid_service_name" {
#  description	= "Name of deid service"
#  type		= string
#  default	= "deid-endpoint"
#}

#variable "organization" {
#  description	= "Organization name (OU) for TLS certificate"
#  type		= string
#  default	= "WatsonHealth"
#}


##Start ORIG config

variable "tsl_cert_namespace" {
  description = "Kubernetes namespace to deploy to."
  type        = string
}

variable "tsl_cert_service_names" {
  description = "List of services names (DNS names) include as ALT names in generated TLS certificate."
  type        = list(string)
}

variable "tsl_cert_organization" {
  description = "Organization name (OU) for TLS certificate"
  type        = string
}

#variable "resource_group" {
#  description = "Resource group"
#  type = string
#}

#variable "kubernetes_config_context" {
#  description = "k8s cluster config context used by the k8s terraform provider"
#  type = string
#}