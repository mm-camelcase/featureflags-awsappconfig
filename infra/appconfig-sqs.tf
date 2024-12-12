resource "aws_sqs_queue" "cc_poc_appconfig" {
  name = "cc-poc-appconfig"
}

resource "aws_sqs_queue_policy" "cc_poc_appconfig" {
  queue_url = aws_sqs_queue.cc_poc_appconfig.id

  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "First",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.cc_poc_appconfig.arn}"
    }
  ]
}
POLICY
}

data "aws_iam_policy_document" "cc_poc_appconfig" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["appconfig.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "cc_poc_appconfig" {
  name               = "cc-poc-appconfig"
  assume_role_policy = data.aws_iam_policy_document.cc_poc_appconfig.json
}

resource "aws_appconfig_extension" "cc_poc_appconfig" {
  name        = "cc-poc-appconfig"
  description = "SQS extension for camelcase AppConfig Poc"

  action_point {
    point = "ON_DEPLOYMENT_START"
    action {
      name     = "cc-poc-appconfig-action-1"
      role_arn = aws_iam_role.cc_poc_appconfig.arn
      uri      = aws_sqs_queue.cc_poc_appconfig.arn
    }
  }

  action_point {
    point = "ON_DEPLOYMENT_STEP"
    action {
      name     = "cc-poc-appconfig-action-2"
      role_arn = aws_iam_role.cc_poc_appconfig.arn
      uri      = aws_sqs_queue.cc_poc_appconfig.arn
    }
  }

  action_point {
    point = "ON_DEPLOYMENT_BAKING"
    action {
      name     = "cc-poc-appconfig-action-3"
      role_arn = aws_iam_role.cc_poc_appconfig.arn
      uri      = aws_sqs_queue.cc_poc_appconfig.arn
    }
  }

  action_point {
    point = "ON_DEPLOYMENT_COMPLETE"
    action {
      name     = "cc-poc-appconfig-action-4"
      role_arn = aws_iam_role.cc_poc_appconfig.arn
      uri      = aws_sqs_queue.cc_poc_appconfig.arn
    }
  }

  action_point {
    point = "ON_DEPLOYMENT_ROLLED_BACK"
    action {
      name     = "cc-poc-appconfig-action-5"
      role_arn = aws_iam_role.cc_poc_appconfig.arn
      uri      = aws_sqs_queue.cc_poc_appconfig.arn
    }
  }

  tags = {
    Type = "AppConfig Extension"
  }
}

resource "aws_appconfig_extension_association" "cc_poc_appconfig" {
  extension_arn = aws_appconfig_extension.cc_poc_appconfig.arn
  resource_arn  = aws_appconfig_application.cc_poc.arn
}

