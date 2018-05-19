package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego
import com.sameei.xtool.elasticreporter.v1.flink.lego.ReporterForMultipleGroups.Ref
import org.apache.flink.metrics.{Metric, MetricGroup}

import scala.collection.JavaConverters._

trait ReporterForJob extends lego.ReporterForMultipleGroups {

    protected def keys: Array[String]

    protected def toGroupId(name : String, metric : Metric, group : MetricGroup): String

    protected def toMetricName(name : String, metric : Metric, group : MetricGroup): String

    override protected def select(
        name : String, metric : Metric, group : MetricGroup
    ) : Option[ReporterForMultipleGroups.Ref] = {

        val vars = group.getAllVariables.asScala

        if (vars.size < keys.size) None
        else {

            val count = keys.foldLeft(0) { (count, k) =>
                if (vars.contains(k)) count + 1
                else count
            }

            if (count < keys.size) None
            else {
                Some(Ref(
                    toGroupId(name, metric, group),
                    toMetricName(name, metric, group)
                ))
            }
        }
    }
}
