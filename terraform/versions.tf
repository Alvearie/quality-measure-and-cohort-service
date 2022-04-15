#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
  
terraform {
  required_version = ">=0.13.6"
  required_providers {
    ibm = {
      source  = "ibm-cloud/ibm"
      version = "~> 1.25.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "> 2.7.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 3.1.0"
    }
  }
}