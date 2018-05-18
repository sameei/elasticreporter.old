package com.sameei.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.{Metric, MetricConfig, MetricGroup}
import org.apache.flink.metrics.reporter.{MetricReporter, Scheduled}

abstract class SingleGroup extends Open with Scheduled { self =>

    private var group: GroupedMetrics = null

    private val removeQueue = collection.mutable.ListBuffer.empty[Metric]

    override def open(config : MetricConfig) : Unit = {
        super.open(config)
        self.group = new GroupedMetrics(s"${name}.group.${System.currentTimeMillis()}")
    }

    override def close() : Unit = {}

    def select(name: String, metric: Metric, group: MetricGroup): Option[String]

    override def notifyOfAddedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = synchronized {
        select(metricName, metric, group) match {
            case Some(key) =>
                self.group.addMetric(key, metricName, metric, group)
                if (logger.isDebugEnabled()) {
                    val id = group.getMetricIdentifier(metricName)
                    logger.debug(s"AddMetric, New Metric : ${metricName}, ID: ${id}")
                }
            case None =>
                if (logger.isDebugEnabled()) {
                    val id = group.getMetricIdentifier(metricName)
                    logger.debug(s"AddMetric, Ignore Metric : ${metricName}, ID: ${id}")
                }
        }
    }

    override def notifyOfRemovedMetric(metric : Metric, metricName : String, group : MetricGroup) : Unit = synchronized {
        self.removeQueue += metric
        if (logger.isDebugEnabled()) logger.debug(s"DropMetric, Queue +: ${name}")
    }

    override def report() : Unit = {
        reporter.apply(group, System.currentTimeMillis())
        if (!removeQueue.isEmpty) synchronized {
            val size = removeQueue.size
            removeQueue.foreach { m => group.dropMetric(m) }
            removeQueue.clear()
            if (logger.isDebugEnabled()) logger.debug(s"DropMetric, Clear Queue: ${size}")
        }
    }
}
