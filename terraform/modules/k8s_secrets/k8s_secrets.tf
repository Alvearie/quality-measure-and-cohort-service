resource "kubernetes_secret" "spark_dockerconfigjson" {
  metadata {
    name = var.service_account_secret_name
    namespace = var.service_account_dockerconfigjson_namespace
  }

  data = {
    ".dockerconfigjson" = var.service_account_dockerconfigjson
  }

  type = "kubernetes.io/dockerconfigjson"
}