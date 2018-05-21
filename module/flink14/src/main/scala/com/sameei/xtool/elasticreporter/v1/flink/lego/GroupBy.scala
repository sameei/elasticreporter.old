package com.sameei.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.MetricConfig
import data._
import scala.collection.JavaConverters._

trait GroupBy { def groupOf(ref: MetricRef): String }

object GroupBy {

    class Nothing extends GroupBy {
        override def groupOf(ref : MetricRef) : String = "THE_SINGLE_GROUP"

        override def toString : String = getClass.getName
    }

    case class Pattern(pattern: String) extends GroupBy {

        override def groupOf(ref : MetricRef) : String = {
            ref.group.getAllVariables.asScala
                .foldLeft(pattern) { case (pt,(k,v)) =>
                    pt.replace(k,v)
                }
        }
    }

    def apply(config: MetricConfig): GroupBy = {
        Option(config.getString("group-by", null)).map { pattern =>
            new Pattern(pattern)
        } getOrElse { new Nothing }
    }
}
