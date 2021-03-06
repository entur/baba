# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.12"
}

provider "google" {
  version = "~> 3.43"
}
provider "kubernetes" {
  load_config_file = var.load_config_file
  version = "~> 1.13.3"
}

# create service account
resource "google_service_account" "baba_service_account" {
  account_id = "${var.labels.team}-${var.labels.app}-sa"
  display_name = "${var.labels.team}-${var.labels.app} service account"
  project = var.gcp_resources_project
}

# add service account as member to the cloudsql client
resource "google_project_iam_member" "project" {
  project = var.gcp_cloudsql_project
  role = var.service_account_cloudsql_role
  member = "serviceAccount:${google_service_account.baba_service_account.email}"
}

# create key for service account
resource "google_service_account_key" "baba_service_account_key" {
  service_account_id = google_service_account.baba_service_account.name
}

# Add SA key to to k8s
resource "kubernetes_secret" "baba_service_account_credentials" {
  metadata {
    name = "${var.labels.team}-${var.labels.app}-sa-key"
    namespace = var.kube_namespace
  }
  data = {
    "credentials.json" = base64decode(google_service_account_key.baba_service_account_key.private_key)
  }
}

resource "kubernetes_secret" "ror-baba-secret" {
  metadata {
    name = "${var.labels.team}-${var.labels.app}-secret"
    namespace = var.kube_namespace
  }

  data = {
    "baba-db-username" = var.ror-baba-db-username
    "baba-db-password" = var.ror-baba-db-password
    "baba-smtp-username" = var.ror-baba-smtp-username
    "baba-smtp-password" = var.ror-baba-smtp-password
    "baba-keycloak-secret" = var.ror-baba-keycloak-secret
    "baba-auth0-secret" = var.ror-baba-auth0-secret

  }
}
