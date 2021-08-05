#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

#############################################################################
#
#  Resource Groups
#
#############################################################################

variable vpc_rg_cloudsvc {
  type        = string
  description = "Resource Group for cloud services"
  default     = "codev-dev1-wdc-cloudsvc"
}

variable vpc_rg_kube {
  type        = string
  description = "Resource Group for IKS"
  default     = "codev-dev1-wdc-kube"
}

variable resource_group {
  type        = string
  description = "Resource Group for cloud services.  Needed by Toolchain."
  default     = "codev-dev1-wdc-cloudsvc"
}

#############################################################################
#
#   Kubernetes
#
#############################################################################

variable cluster_name {
  type        = string
  description = "Name of the Cluster deployed by WH Automation"
  default     = "cohort-dev-1"
}

#############################################################################
#
# Kubernetes signed certificate creation for REST API server
#
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
#############################################################################
=======
#
###############################################################################

#variable "configure_namespace" {
#  type = list
#  description = "Configures new namespace"
#  default     = ["monitoring","msn-observe"]
#  default     = ["cicdtest"]
#}

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
variable "service_account_dockerconfigjson" {
  description = "Dockerconfigjson value used to pull spark images from container registry namespace"
  type = string
}

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

variable "spark_cr_namespace_name" {
  type          = string
  description   = "Name of the container registry namespace to contain spark images"
  default       = "vpc-dev-cohort-rns-spark"
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

>>>>>>> 9a36a08 more changes
=======
#############################################################################
>>>>>>> b62ea50 spark on k8s terraform config
## -----------------------------------------------------------------------------------------
# tls-cert
## -----------------------------------------------------------------------------------------

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

#############################################################################
#
#   Kubernetes Namespaces
#
#############################################################################
## -----------------------------------------------------------------------------------------
# Define already existing Namespaces
## -----------------------------------------------------------------------------------------

variable spark_kubernetes_namespace {
  type        = string
  description = "Name of the namespace for K8s Applications"
  default     = "cicdtest"
}

#############################################################################
#
#   Kubernetes Secrets
#
#############################################################################

## -----------------------------------------------------------------------------------------
# K8s Spark imagePullSecret
## -----------------------------------------------------------------------------------------

variable "image_pull_secret_name" {
  description = "Secret name used to pull spark images from container registry namespace"
  type = string
  default = "spark-image-pull-secret"
}

# Dockerconfigjson value used to pull spark images from container registry namespace
# The < o - ###> notation indicated that the value will be substituted by the IBM toolchain
# coming from an env. variable or a value stored within keyProtect. dockerconfigjson
# is stored as an env. variable
# if not using the IBM toolchain, you must manually create a dockerconfig json using the following 
# command
# kubectl create secret docker-registry regcred --dry-run -o yaml <provide your docker registry parms> 
# and taking the value starting after
#data:
#      .dockerconfigjson: <USE_ENCODED_STRING_LOCATED_HERE>
variable "image_pull_secret_dockerconfigjson" {
  description = "Dockerconfigjson value used to pull spark images from container registry namespace"
  type = string
}

## -----------------------------------------------------------------------------------------
# K8s Spark Cloud Object Store Secret
## -----------------------------------------------------------------------------------------

variable "spark_cos_secret_name" {
  description = "Name of the secret used to access Cloud Object Store"
  type = string
  default = "spark-cos-secret"
}

variable "spark_cos_aws_secret_key" {
  description = "AWS secret key value used to access Cloud Object Store"
  type = string
}
variable "spark_cos_aws_access_key" {
  description = "AWS access key value used to access Cloud Object Store"
  type = string
}
variable "spark_cos_aws_endpoint" {
  description = "AWS endpoint value used to access Cloud Object Store"
  type = string
}
variable "spark_cos_aws_location" {
  description = "AWS location value used to access Cloud Object Store"
  type = string
}

##############################################################################
#
# Container registry namespace creation
#
##############################################################################
# Uncomment if you want terraform to create a separate container registry namespace
# to contain your spark images
## -----------------------------------------------------------------------------------------
# Container Registry Namespace Used to Hold Spark Images
## -----------------------------------------------------------------------------------------

#variable "spark_cr_namespace_name" {
#  type          = string
#  description   = "Name of the container registry namespace to contain spark images"
#  default       = "vpc-dev-cohort-rns-spark"
#}

#variable "cr_ns_region" {
#  type          = string
#  description   = "Region for the container registry namespace.  NB:  us-east not available"
#  default       = "us-south"
#}