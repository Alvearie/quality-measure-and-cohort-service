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

provider "ibm" {
  region = var.cr_ns_region
  ibmcloud_timeout = 60
  # provider version dependancy
  version = "~> 1.2"
}
