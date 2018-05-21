package com.sameei.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.{Metric, MetricGroup}

object data {

    trait BaseException extends com.sameei.xtool.elasticreporter.v1.common.data.BaseException

    case class ConfigException(
        desc: String,
        key: String
    ) extends RuntimeException(desc)

    case class InitException(
        desc: String,
        cause: Option[Throwable]
    ) extends RuntimeException(desc, cause.orNull)


    case class MetricRef(name: String, metric: Metric, group: MetricGroup) {
        def desc: String = s"Name: ${name}, Metric: ${metric.getClass.getName}, ID: ${group.getMetricIdentifier(name)}"
    }

    case class Selected(groupId: String, metricKey: String)

    case class MetricRefPlus(ref: Selected, name: String, value: Metric, group: MetricGroup)

}
