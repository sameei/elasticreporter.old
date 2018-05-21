package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego.data.MetricRef
import org.apache.flink.metrics.MetricConfig
import scala.collection.JavaConverters._

trait FilterBy { def filter(ref: MetricRef): Boolean }

object FilterBy {

    class Nothing extends FilterBy {
        override def filter(ref : MetricRef) : Boolean = true

        override def toString : String = getClass.getName
    }

    case class Scope(pattern: String) extends FilterBy {

        override def filter(ref : MetricRef) : Boolean = {

            val requiredScope =
                ref.group.getAllVariables.asScala
                    .foldLeft(pattern) { case (pt,(k,v)) =>
                        pt.replace(k,v)
                    }

            val currentScope = ref.group.getScopeComponents.mkString(".")

            currentScope.startsWith(requiredScope)
        }
    }


    case class RequiredVariables(pattern: String) extends FilterBy {

        val keys = pattern.split(".").toSet

        override def filter(ref : MetricRef) : Boolean = {

            val vars = ref.group.getAllVariables.asScala

            if (vars.size < keys.size) false
            else {

                val count = keys.foldLeft(0) { (count, k) =>
                    if (vars.contains(k)) count + 1
                    else count
                }

                count == keys.size
            }

        }
    }

    def apply(config: MetricConfig): FilterBy = {

        Option(config.getString("filter-by.required-vars", null)).map { vars =>
            new RequiredVariables(vars)
        }.orElse {
            Option(config.getString("filter-by.scope", null)).map { pattern =>
                new Scope(pattern)
            }
        }.getOrElse { new Nothing }
    }

}
