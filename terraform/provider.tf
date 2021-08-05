#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
provider "ibm" {
  ibmcloud_timeout = 60
  # provider version dependancy
  version = "~> 1.25.0"
}