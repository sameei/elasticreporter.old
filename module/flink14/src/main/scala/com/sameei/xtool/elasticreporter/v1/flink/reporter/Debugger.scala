package com.sameei.xtool.elasticreporter.v1.flink.reporter

import java.util.function.BiConsumer

import com.sameei.xtool.elasticreporter.v1.flink.lego.Open
import org.apache.flink.metrics._
import com.sameei.xtool.elasticreporter.v1.common
import com.sameei.xtool.elasticreporter.v1.flink.lego
import com.sameei.xtool.elasticreporter.v1.flink.reporter.Debugger.MetricGM
import org.apache.flink.metrics.reporter.MetricReporter
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class Debugger extends Open {

    override def notifyOfAddedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = {
        val id = group.getMetricIdentifier(metricName)
        val time = System.currentTimeMillis()
        reporter.apply(new MetricGM(id,"AddMetric",metricName, metric, group), time)
    }

    override def notifyOfRemovedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = {
        val id = group.getMetricIdentifier(metricName)
        val time = System.currentTimeMillis()
        reporter.apply(new MetricGM(id,"DropMetric",metricName, metric, group), time)
    }
}

object Debugger {

    case class Ref(name: String, metric: Metric, group: MetricGroup)

    class MetricGM(
        val id: String,
        subject: String,
        name: String,
        metric: Metric,
        group: MetricGroup
    ) extends common.GroupedMetrics with lego.Formatters {

        /*protected def formatMetric[F <: common.Formatter](
            name: String, metric: Metric, group: MetricGroup
        )(implicit formatter: F): Seq[formatter.Val] = {
            val id = group.getMetricIdentifier(name)
            metric match {
                case counter:Counter => formatCounter(id, counter)(formatter) :: Nil
                case gauge: Gauge[_] => formatGauge(id, gauge)(formatter) :: Nil
                case histogram: Histogram => formatHistogram(id, histogram)(formatter)
                case meter: Meter => formatMeter(id, meter)(formatter)
            }
        }*/

        protected def formatMetric[F <: common.Formatter](
            name: String, metric: Metric, group: MetricGroup
        )(implicit formatter: F): Seq[formatter.Val] = {

            val list = collection.mutable.ListBuffer.empty[formatter.Val]

            list += formatter.formatString("@metric.class", metric.getClass.getName)

            list += formatter.formatInt("@metric.vars", group.getAllVariables.size)
            group.getAllVariables.asScala.foreach { case (k,v) =>
                    list += formatter.formatString(s"@metric.var.${k}", v)
            }

            list += formatter.formatInt("@metric.scopes", group.getScopeComponents.size)
            group.getScopeComponents.zipWithIndex.foreach { case (s, i) =>
                    list += formatter.formatString(s"@metric.scope.index_${i}", s)
            }

            // ?
            list += formatter.formatString("@metric.id", id)
            list += formatter.formatString("@metric.name", name)
            list += formatter.formatString("@metric.subject", subject)

            list.toList
        }

        override def metrics[C <: common.ReportContext](context : C) : Seq[context.formatter.Val] = {
            formatMetric(name, metric, group)(context.formatter)
        }

        override def vars[C <: common.ReportContext](context : C) : Map[String, String] = Map.empty
    }

    class PrintConfig extends MetricReporter {

        protected val logger = LoggerFactory.getLogger(getClass)

        override def open(config : MetricConfig) : Unit = {
            config.forEach(new BiConsumer[Object,Object] {
                override def accept(k : Object, v : Object) : Unit =
                    logger.info(s"Open, Key(${k.getClass.getName}): ${k}, Value(${v.getClass.getName}): ${v}")
            })
        }

        override def close() : Unit = {}

        override def notifyOfAddedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = {}

        override def notifyOfRemovedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = {}
    }

}
