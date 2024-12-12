resource "aws_appconfig_application" "cc_poc" {
  name        = "cc-poc-app"
  description = "Integrations PoC Application"

  tags = {
    Type = "AppConfig Application"
  }
}


resource "aws_appconfig_environment" "cc_poc" {
  name           = var.environment
  description    = "Integrations PoC AppConfig Environment"
  application_id = aws_appconfig_application.cc_poc.id

  monitor {
    alarm_arn = "arn:aws:cloudwatch:${var.region}:${data.aws_caller_identity.current.account_id}:alarm:${var.rollback_alarm_name}"
    #alarm_role_arn = data.aws_iam_role.cc_poc_rollback.arn
    alarm_role_arn = aws_iam_role.cc_poc_rollback.arn
  }

  tags = {
    Type = "AppConfig Environment"
  }
}

resource "aws_appconfig_configuration_profile" "cc_poc_sandbox" {
  application_id = aws_appconfig_application.cc_poc.id
  description    = "Integrations PoC sandbox Profile"
  name           = "cc-poc-sandbox-profile"
  location_uri   = "hosted"
  type           = "AWS.AppConfig.FeatureFlags"

  #  validator {
  #    content = aws_lambda_function.example.arn
  #    type    = "LAMBDA"
  #  }

  tags = {
    Type = "AppConfig Configuration Profile"
  }

}

resource "aws_appconfig_configuration_profile" "cc_poc_live" {
  application_id = aws_appconfig_application.cc_poc.id
  description    = "Integrations PoC live Profile"
  name           = "cc-poc-live-profile"
  location_uri   = "hosted"
  type           = "AWS.AppConfig.FeatureFlags"

  #  validator {
  #    content = aws_lambda_function.example.arn
  #    type    = "LAMBDA"
  #  }

  tags = {
    Type = "AppConfig Configuration Profile"
  }

}

resource "aws_appconfig_hosted_configuration_version" "cc_poc_sandbox" {
  application_id           = aws_appconfig_application.cc_poc.id
  configuration_profile_id = aws_appconfig_configuration_profile.cc_poc_sandbox.configuration_profile_id
  description              = "PoC Feature Flag Configuration Version"
  content_type             = "application/json"

  content = file("${path.module}/templates/feature-flag-config.tpl")

}

resource "aws_appconfig_hosted_configuration_version" "cc_poc_live" {
  application_id           = aws_appconfig_application.cc_poc.id
  configuration_profile_id = aws_appconfig_configuration_profile.cc_poc_live.configuration_profile_id
  description              = "PoC Feature Flag Configuration Version"
  content_type             = "application/json"

  content = file("${path.module}/templates/feature-flag-config.tpl")

}

# breaks on updates if already exists (no data element to check)
#resource "aws_appconfig_deployment" "cc_poc" {
#  application_id           = aws_appconfig_application.cc_poc.id
#  configuration_profile_id = aws_appconfig_configuration_profile.cc_poc_sandbox.configuration_profile_id
#  configuration_version    = aws_appconfig_hosted_configuration_version.cc_poc_sandbox.version_number
#  deployment_strategy_id   = "AppConfig.AllAtOnce"
#  description              = "PoC initial deployment"
#  environment_id           = aws_appconfig_environment.cc_poc.environment_id
#
#  tags = {
#    Type = "AppConfig Deployment"
#  }
#}


