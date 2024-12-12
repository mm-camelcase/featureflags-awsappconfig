data "aws_cloudwatch_log_group" "cc_poc" {
  name = "/aws/ecs/camelcase/poc/synchronous-service"

  depends_on = [
    module.ecs_service_pipeline
  ]
}

resource "aws_cloudwatch_log_metric_filter" "cc_poc" {
  name           = var.rollback_alarm_name
  pattern        = "ERROR"
  log_group_name = data.aws_cloudwatch_log_group.cc_poc.name

  metric_transformation {
    name          = var.rollback_alarm_name
    namespace     = "cc-poc-sync"
    value         = "1"
    default_value = "0"
  }
}

resource "aws_cloudwatch_metric_alarm" "cc_poc" {
  alarm_name          = var.rollback_alarm_name
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  metric_name         = aws_cloudwatch_log_metric_filter.cc_poc.name
  namespace           = "cc-poc-sync"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "1"
  alarm_description   = "This metric monitors log errors"
  datapoints_to_alarm = 1
  treat_missing_data  = "notBreaching"
}