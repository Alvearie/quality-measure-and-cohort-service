#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
provider "ibm" {
  region = var.ibm_region
  ibmcloud_timeout = 60
  # provider version dependancy
  version = "~> 1.2"
}

#provider "kubernetes" {
#    config_path = "~/.kube/config"
#    config_context = var.kubernetes_config_context
#}