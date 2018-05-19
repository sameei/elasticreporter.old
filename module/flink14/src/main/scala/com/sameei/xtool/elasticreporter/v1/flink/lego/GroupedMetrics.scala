package com.sameei.xtool.elasticreporter.v1.flink.lego
import com.sameei.xtool.elasticreporter.v1.common.ReportContext
import org.apache.flink.metrics._
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._


class GroupedMetrics(
    override val id: String
) extends com.sameei.xtool.elasticreporter.v1.common.GroupedMetrics with Formatters {

    protected val logger = LoggerFactory getLogger id

    protected case class MetricRep(key: String, name: String, metric: Metric, group: MetricGroup) {
        val metricId = group.getMetricIdentifier(name)
    }

    protected case class VarRep(name: String, value: String, var count: Int)

    protected def empty[T] = collection.mutable.HashMap.empty[T, MetricRep]

    protected val gauges = empty[Gauge[_]]
    protected val counters = empty[Counter]
    protected val histograms = empty[Histogram]
    protected val meters = empty[Meter]

    protected val byKey = empty[String]

    protected val vars = collection.mutable.HashMap.empty[String, VarRep]

    logger.info(s"Init, Class: ${getClass.getName}")

    def count = byKey.size

    def isEmpty = byKey.isEmpty

    protected def addVar(key: String, value: String): Unit = {
        vars.get(key) match {
            case None =>
                vars.put(key, VarRep(key, value, 0))
                if (logger.isDebugEnabled()) logger.debug(s"AddVar, Key: ${key}, Val: ${value}")
            case Some(current) if current.value == value => current.count += 1
            case Some(current) =>
                logger.warn(s"AddVar, Simillar Key Different Values, Key: '${key}', Old: '${current.value}', New: '${value}', IGNORE NEW!")
        }
    }

    protected def dropVar(key: String): Unit = {
        vars.get(key) match {
            case Some(current) if current.count == 1 =>
                vars.remove(key)
                if (logger.isDebugEnabled()) logger.debug(s"AddVar, Key: ${key}, Val: ${current.value}")
            case Some(current) => current.count -= 1
            case _ =>
        }
    }

    def addMetric(key: String, name: String, metric: Metric, group: MetricGroup) = {

        byKey.get(key) match {
            case Some(v) =>
                val id = group.getMetricIdentifier(name)
                logger.warn(s"AddMetric, Key Conflict: '${key}', Old: ${v.metricId}, New: ${id}, IGNORE NEW!")

            case None =>

                val rp = MetricRep(key, name, metric, group)

                val hasBeenAdded = metric match {
                    case counter: Counter => counters.put(counter, rp); true
                    case gauge: Gauge[_] => gauges.put(gauge, rp); true
                    case histogram: Histogram => histograms.put(histogram, rp); true
                    case meter: Meter => meters.put(meter, rp); true
                    case _ => false
                }

                if (hasBeenAdded) {

                    byKey.put(key, rp)

                    val vars = group.getAllVariables.asScala.toSeq
                    vars.foreach { case (k,v) => addVar(k, v) }

                    if (logger.isDebugEnabled()){
                        val id = group.getMetricIdentifier(name)
                        logger.debug(s"AddMetric, Key: ${key}, Vars: ${vars.size}, Class: ${metric.getClass.getName}, ID: ${id},")
                    }

                } else logger.warn(s"AddMEtric, Unexpected Class: ${metric.getClass.getName}")
        }
    }

    def dropMetric(metric: Metric) : Unit = {

        val rp = metric match {
            case counter: Counter => counters.remove(counter)
            case gauge: Gauge[_] => gauges.remove(gauge)
            case histogram: Histogram => histograms.remove(histogram)
            case meter: Meter => meters.remove(meter)
            case _ => None
        }

        if (rp.isDefined) {

            val value = rp.get
            val vars = value.group.getAllVariables.asScala.toSeq
            vars.foreach { case (k,v) => dropVar(k) }

            byKey.remove(value.key)

            if (logger.isDebugEnabled()) {
                val id = value.group.getMetricIdentifier(value.name)
                logger.debug(s"DropMetric, Key: ${value.key}, Vars: ${value.group.getAllVariables.size}, Class: ${value.metric.getClass.getName}, ID: ${id}")

            }

        } else if (logger.isDebugEnabled) logger.debug(s"DropMetric, Not Found: (${metric.getClass.getName}, ${metric})")
    }

    override def metrics[C <: ReportContext](context : C) : Seq[context.formatter.Val] = {

        val list = collection.mutable.ListBuffer.empty[context.formatter.Val]

        list ++= counters.map { case (m,v) => formatCounter(v.key, m)(context.formatter) }

        list ++= gauges.map { case(m,v) => formatGauge(v.key, m)(context.formatter) }

        list ++= histograms.map { case (m,v) => formatHistogram(v.key, m)(context.formatter) }.flatMap { i => i }

        list ++= meters.map { case(m,v) => formatMeter(v.key, m)(context.formatter) }.flatMap { i => i}

        val result = list.toList

        if (logger.isDebugEnabled) {
            logger.debug(s"Metrics, Count: ${result.size}, Context: ${context.id}")

            if (logger.isTraceEnabled) logger.trace(s"Values(${result.size}): ${result}, Context: ${context.id}")
        }


        result
    }

    override def vars[C <: ReportContext](context : C) : Map[String, String] = {
        val result = vars.map { case (k,v) => k -> v.value }.toMap

        if (logger.isDebugEnabled()) {
            logger.debug(s"Vars, Count: ${result.size}, Context: ${context.id}")

            if (logger.isTraceEnabled) logger.trace(s"Vars(${result.size}): ${result}, Context: ${context.id}")
        }

        result
    }
}
