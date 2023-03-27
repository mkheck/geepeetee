variable "location" {
  description = "Datacenter location"
  default     = "eastus"
}

variable "application_openai_key" {
  description = "Key for OpenAI instance in Azure"
}

variable "application_openai_deployment" {
  description = "Identifier for OpenAI deployment in Azure"
}

variable "application_openai_url" {
  description = "URL for OpenAI instance in Azure"
}
