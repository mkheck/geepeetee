terraform {
  required_providers {
    azurerm = {
      version = "=3.45.0"
    }
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = true
      recover_soft_deleted_key_vaults = true
    }
  }
}

# From https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/key_vault
data "azurerm_client_config" "current" {}

resource "azurerm_resource_group" "geepeetee" {
  location = var.location
#  name     = "${var.rootname}-rg"
  name     = "${var.rootname}-rg"

  tags = {
    "Terraform" = "true"
  }
}

resource "azurerm_service_plan" "geepeetee" {
  name                = "${var.rootname}-plan"
  resource_group_name = azurerm_resource_group.geepeetee.name
  location            = azurerm_resource_group.geepeetee.location
  os_type             = "Linux"
  sku_name            = "B1"
}

resource "azurerm_linux_web_app" "geepeetee" {
  name                = "${var.rootname}-app"
  resource_group_name = azurerm_resource_group.geepeetee.name
  location            = azurerm_service_plan.geepeetee.location
  service_plan_id     = azurerm_service_plan.geepeetee.id

  https_only = true

  site_config {
    #    linux_fx_version = "JAVA:17-java17"
    #    MH: Determine which of the following are *actually* necessary, eliminate any others
    application_stack {
      java_server = "JAVA"
      java_server_version = "17"
      java_version = "17"
    }
  }

  app_settings = {
    #    Here is where you place Azure Webapp/Spring Boot properties to pass into the app
    #    "SPRING_PROFILES_ACTIVE" = "mysql"
    "WEBSITES_PORT" = "8080"
  }
}

resource "azurerm_key_vault" "geepeetee" {
  name                        = "${var.rootname}-kv"
  location                    = azurerm_resource_group.geepeetee.location
  resource_group_name         = azurerm_resource_group.geepeetee.name
  enabled_for_disk_encryption = true
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false

  sku_name = "standard"

  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id

    key_permissions = [
      "Create",
      "Get",
    ]

    secret_permissions = [
      "Set",
      "Get",
      "Delete",
      "Purge",
      "Recover"
    ]

    storage_permissions = [
      "Get",
    ]
  }
}

resource "azurerm_key_vault_secret" "geepeetee-key" {
  name         = "application-openai-key"
  value        = "${var.application_openai_key}"
  key_vault_id = azurerm_key_vault.geepeetee.id
}

resource "azurerm_key_vault_secret" "geepeetee-dep" {
  name         = "application-openai-deployment"
  value        = "${var.application_openai_deployment}"
  key_vault_id = azurerm_key_vault.geepeetee.id
}

resource "azurerm_key_vault_secret" "geepeetee-url" {
  name         = "application-openai-url"
  value        = "${var.application_openai_url}"
  key_vault_id = azurerm_key_vault.geepeetee.id
}
