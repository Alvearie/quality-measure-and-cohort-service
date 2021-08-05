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
#############################################################################
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