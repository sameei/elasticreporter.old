package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego
import data._
import org.apache.flink.metrics.{Metric, MetricGroup}

import scala.collection.JavaConverters._

trait ReporterForJob extends lego.ReporterForMultipleGroups {

    protected def keys: Array[String]

    protected def toGroupId(ref: FlinkMetricRef): String

    protected def toMetricName(ref: FlinkMetricRef): String

    override protected def select(
        ref: FlinkMetricRef
    ) : Option[Selected] = {

        val vars = ref.group.getAllVariables.asScala

        if (vars.size < keys.size) None
        else {

            val count = keys.foldLeft(0) { (count, k) =>
                if (vars.contains(k)) count + 1
                else count
            }

            if (count < keys.size) None
            else {
                Some(Selected(
                    toGroupId(ref),
                    toMetricName(ref)
                ))
            }
        }
    }
}
