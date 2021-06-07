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
output "certificates" {
  description = "The public certificates for each service."
  value       = { for key, value in kubernetes_certificate_signing_request.csr : (key) => value.certificate }
}
