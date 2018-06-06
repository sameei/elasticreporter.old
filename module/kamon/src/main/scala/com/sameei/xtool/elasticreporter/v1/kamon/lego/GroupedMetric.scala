package com.sameei.xtool.elasticreporter.v1.kamon.lego

import com.sameei.xtool.elasticreporter.v1.common.{GroupedMetrics, ReportContext}
import kamon.metric._

case class GroupedMetric(
    val id: String,
    val snapshot: MetricsSnapshot
) extends com.sameei.xtool.elasticreporter.v1.common.GroupedMetrics {

    def formatValue[C <: ReportContext](
        metric: MetricValue
    )(implicit context: C): context.formatter.Val = {
        context.formatter.formatLong(metric.name, metric.value)
    }

    def formatDistribution[C <: ReportContext](
        metric: MetricDistribution
    )(implicit context: C): Seq[context.formatter.Val] = {

        val percentile50 = metric.distribution.percentile(0.5)
        val percentile75 = metric.distribution.percentile(0.75)
        val percentile90 = metric.distribution.percentile(0.9)
        val percentile95 = metric.distribution.percentile(0.95)
        val percentile99 = metric.distribution.percentile(0.99)

        Seq(
            context.formatter.formatLong(s"${metric.name}.count", metric.distribution.count),
            context.formatter.formatLong(s"${metric.name}.max", metric.distribution.max),
            context.formatter.formatLong(s"${metric.name}.min", metric.distribution.min),
            context.formatter.formatDouble(s"${metric.name}.p50.value", percentile50.value),
            context.formatter.formatDouble(s"${metric.name}.p75.value", percentile75.value),
            context.formatter.formatDouble(s"${metric.name}.p90.value", percentile90.value),
            context.formatter.formatDouble(s"${metric.name}.p95.value", percentile95.value),
            context.formatter.formatDouble(s"${metric.name}.p99.value", percentile99.value)
        )
    }

    override def metrics[C <: ReportContext](context : C) : Seq[context.formatter.Val] = {

        /*
        [error]  found   : Seq[_c.formatter.Val]
        [error]  required: scala.collection.TraversableOnce[context.formatter.Val]
        */

        val all = collection.mutable.ListBuffer.empty[context.formatter.Val]

        all.appendAll(snapshot.counters.map { c => formatValue(c)(context) })

        all.appendAll(snapshot.gauges.map { g => formatValue(g)(context) })

        all.appendAll(snapshot.histograms.flatMap { hs => formatDistribution(hs)(context) })

        all.appendAll(snapshot.rangeSamplers.flatMap { rs => formatDistribution(rs)(context) })

        all.toList
    }

    override def vars[C <: ReportContext](context : C) : Map[String, String] = Map.empty
}
