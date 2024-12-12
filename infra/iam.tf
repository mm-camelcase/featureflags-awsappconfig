data "aws_iam_policy_document" "ecs_tasks" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_synchronous_service_role" {
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks.json
  name               = "ecs-synchronous-service-role${local.name_suffix}${local.iam_resource_name_extra_str}"
  tags = {
    Name = "ecs-synchronous-service-role"
  }
}

data "template_file" "ecs_synchronous_service_role" {
  template = file("${path.module}/templates/ecs-role-policy.json.tpl")
}


resource "aws_iam_role_policy" "ecs_synchronous_service_role" {
  name   = "ecs-slack-web-role${local.name_suffix}${local.iam_resource_name_extra_str}"
  policy = data.template_file.ecs_synchronous_service_role.rendered
  role   = aws_iam_role.ecs_synchronous_service_role.name
}

resource "aws_iam_role_policy_attachment" "ecs_exec_base" {
  policy_arn = data.terraform_remote_state.ecs.outputs.ecs-exec-base-policy-arn
  role       = aws_iam_role.ecs_synchronous_service_role.name
}


############## Allow appconfig rollbacks based on cloudwatch alarms ################

resource "aws_iam_policy" "cc_poc_rollback" {
  name = "cc-poc-rollback-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "cloudwatch:DescribeAlarms",
        ]
        Effect   = "Allow"
        Resource = "*"
      },
    ]
  })
}

resource "aws_iam_role" "cc_poc_rollback" {
  name = "cc-poc-rollback-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "appconfig.amazonaws.com"
        }
      },
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cc_poc_rollback" {
  policy_arn = aws_iam_policy.cc_poc_rollback.arn
  role       = aws_iam_role.cc_poc_rollback.name
}
