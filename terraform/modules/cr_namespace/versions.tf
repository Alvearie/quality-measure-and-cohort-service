#############################################################################
##
## IBM Confidential
## OCO Source Materials
## Copyright IBM Corporation 2020 - 2021
## The source code for this program is not published or otherwise
## divested of its trade secrets, irrespective of what has been
## deposited with the U.S. Copyright Office.
##
###########################################################################

terraform {
  required_version = "0.13.6"
  required_providers {
    ibm = {
      source  = "ibm-cloud/ibm"
      version = "~> 1.25.0"
    }
  }
}
