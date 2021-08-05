#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
  
terraform {
  required_version = "0.13.6"
  required_providers {
    ibm = {
      source  = "ibm-cloud/ibm"
      version = "~> 1.25.0"
    }
    # The 0.13.6 version of terraform used by IBM Cloud toolchains
    # is not compatible with the latest versions of the k8s provider
    # (ie 2.4.0 or later) so we need to specify the version to avoid
    # the toolchain downloading the latest version which will fail due
    # to the incompatibility
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