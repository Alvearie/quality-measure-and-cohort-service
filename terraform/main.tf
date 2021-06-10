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

# key used to generate Certificate Request
resource "tls_private_key" "private_key" {
  algorithm   = "ECDSA"
  ecdsa_curve = "P384"
}

locals {
  dns_names = concat([for service in var.service_names : [service, "${service}.${var.namespace}", "${service}.${var.namespace}.svc"]]...)
}

# Construct the CSR
resource "tls_cert_request" "cert_request" {
  for_each = toset(var.service_names)

  key_algorithm   = "ECDSA"
  private_key_pem = tls_private_key.private_key.private_key_pem
  dns_names       = [each.key, "${each.key}.${var.namespace}", "${each.key}.${var.namespace}.svc"]

  subject {
    common_name  = each.key
    organization = var.organization
  }
}

# Issue the certificate signing request
resource "kubernetes_certificate_signing_request" "csr" {
  for_each = toset(var.service_names)

  metadata {
    generate_name = "${each.key}-csr"
  }

  spec {
    usages  = ["client auth", "server auth"]
    request = tls_cert_request.cert_request[each.key].cert_request_pem
  }
  auto_approve = true
}

data "kubernetes_namespace" "namespace" {
  metadata {
    name = var.namespace
  }
}

# Get the signed certificate from the request and save it in the secret
resource "kubernetes_secret" "secret" {
  for_each = toset(var.service_names)

  metadata {
    name      = "${each.key}-tls"
    namespace = data.kubernetes_namespace.namespace.metadata[0].name
  }
  data = {
    "tls.crt" = kubernetes_certificate_signing_request.csr[each.key].certificate
    "tls.key" = tls_private_key.private_key.private_key_pem
  }
  type = "kubernetes.io/tls"
}
