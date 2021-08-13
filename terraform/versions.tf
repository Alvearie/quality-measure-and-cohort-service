# *****************************************************************
#
# Licensed Materials - Property of IBM
#
# (C) Copyright IBM Corp. 2021. All Rights Reserved.
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# *****************************************************************
  
terraform {
  required_version = ">=0.13.6"
  required_providers {
    ibm = {
      source  = "ibm-cloud/ibm"
      version = "~> 1.25.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.3.2"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 3.1.0"
    }
  }
}

provider "ibm" {
}

provider "kubernetes" {
    config_path = "~/.kube/config"
    config_context = var.kubernetes_config_context
}