package com.sameei.xtool.elasticreporter.v1.flink

import com.sameei.xtool.elasticreporter.v1.flink.lego._
import com.sameei.xtool.elasticreporter.v1.flink.lego.data._
import org.apache.flink.metrics.MetricConfig

class Reporter extends Open with ReporterForMultipleGroups {

    protected var groupBy : GroupBy = null

    protected var filterBy : FilterBy = null

    protected var nameBy : NameBy = null

    override def open(config : MetricConfig) : Unit = {

        super.open(config)

        groupBy = GroupBy(config)

        filterBy = FilterBy(config)

        nameBy = NameBy(config)

        logger.info(s"Open, GroupBy: ${groupBy}, FilterBy: ${filterBy}, NameBy: ${nameBy}")
    }

    override protected def select(ref: MetricRef) : Option[Selected] = {

        if (!filterBy.filter(ref)) {
            logger.info(s"Filter, ${ref.desc}")
            None
        } else {

            val groupId = groupBy.groupOf(ref)
            val metricKey = nameBy.nemeOf(ref)

            logger.info(s"Select, GroupID: ${groupId}, MetricKey: ${metricKey}, ${ref}")

            Some(Selected(groupId, metricKey))
        }
    }
}
