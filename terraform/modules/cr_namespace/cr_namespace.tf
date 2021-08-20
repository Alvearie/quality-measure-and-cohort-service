#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
#the container registry namespace used to contain the spark images
resource "ibm_cr_namespace" "spark_cr_namespace" {
  name              = var.cr_namespace_name
  resource_group_id = var.resource_group_id 
}
