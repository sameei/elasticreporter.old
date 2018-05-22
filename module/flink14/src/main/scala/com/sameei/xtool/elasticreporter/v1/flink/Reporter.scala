package com.sameei.xtool.elasticreporter.v1.flink

import com.sameei.xtool.elasticreporter.v1.flink.lego._
import com.sameei.xtool.elasticreporter.v1.flink.lego.data._
import org.apache.flink.metrics.MetricConfig

class Reporter extends Open with ReporterForMultipleGroups {

    protected var groupBy : GroupBy = null

    protected var filters : Seq[FilterBy] = null

    protected var nameBy : NameBy = null

    override def open(config : MetricConfig) : Unit = {

        super.open(config)

        filters = FilterBy(config)

        groupBy = GroupBy(config)

        nameBy = NameBy(config)

        logger.info(s"Open, FilterBy: ${filters}, GroupBy: ${groupBy}, NameBy: ${nameBy}")
    }

    override protected def select(ref: FlinkMetricRef) : Option[Selected] = {

        FilterBy(filters, ref) match {

            case Some(filter) =>
                logger.info(s"Filter, ${filter}, ${ref.desc}")
                None

            case None =>

                val groupId = groupBy.groupOf(ref)
                val metricKey = nameBy.nemeOf(ref)

                logger.info(s"Select, GroupID: ${groupId}, MetricKey: ${metricKey}, ${ref.desc}")

                Some(Selected(groupId, metricKey))
        }
    }
}
