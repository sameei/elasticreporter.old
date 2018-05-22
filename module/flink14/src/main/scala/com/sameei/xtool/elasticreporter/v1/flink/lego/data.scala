package com.sameei.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.{Metric, MetricGroup}
import scala.collection.JavaConverters._

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


    trait MetricRef {
        def name: String
        def id: String
        def vars: Map[String, String]
        def scope: Seq[String]
        def desc: String
    }

    case class FlinkMetricRef(name: String, metric: Metric, group: MetricGroup) extends MetricRef {
        lazy val id: String = group.getMetricIdentifier(name)
        lazy val vars: Map[String, String] = group.getAllVariables.asScala.toMap
        lazy val scope: Seq[String] = group.getScopeComponents.toList
        def desc: String = s"Metric: ${metric.getClass.getName}, Name: ${name}, Id: ${id}, Vars: ${vars}, Scope: ${scope}"
    }

    case class FakeMetricRef(
        name: String, id: String, vars: Map[String, String], scope: Seq[String]
    ) extends MetricRef {
        def desc: String = s"Fake, Name: ${name}, Id: ${id}, Vars: ${vars}, Scope: ${scope}"
    }

    case class Selected(groupId: String, metricKey: String)

    case class MetricRefPlus(ref: Selected, name: String, value: Metric, group: MetricGroup)

}
