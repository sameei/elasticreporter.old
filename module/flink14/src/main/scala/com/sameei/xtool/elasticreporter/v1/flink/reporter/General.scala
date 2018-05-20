package com.sameei.xtool.elasticreporter.v1.flink.reporter

import com.sameei.xtool.elasticreporter.v1.flink.lego.{GroupedMetrics, Open, ReporterForMultipleGroups}
import com.sameei.xtool.elasticreporter.v1.flink.reporter.General.{FilterBy, GroupBy, MetricRef, NameBy}
import org.apache.flink.metrics.{Metric, MetricConfig, MetricGroup}

import scala.collection.JavaConverters._
import scala.collection.mutable

class General extends Open with ReporterForMultipleGroups {

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

    override protected def select(
        name : String, metric : Metric, group : MetricGroup
    ) : Option[ReporterForMultipleGroups.Ref] = {

        val ref = MetricRef(name, metric, group)

        if (!filterBy.filter(ref)) {
            logger.info(s"Filter, ${ref.desc}")
            None
        } else {

            val groupId = groupBy.groupOf(ref)
            val metricKey = nameBy.nemeOf(ref)

            logger.info(s"Select, GroupID: ${groupId}, MetricKey: ${metricKey}, ${ref}")

            Some(ReporterForMultipleGroups.Ref(
                groupId, metricKey
            ))
        }
    }
}

object General {

    case class MetricRef(name: String, metric: Metric, group: MetricGroup) {
        def desc: String = s"Name: ${name}, Metric: ${metric.getClass.getName}, ID: ${group.getMetricIdentifier(name)}"
    }

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


    trait NameBy { def nemeOf(ref: MetricRef): String }

    object NameBy {

        class Origin extends NameBy {
            override def nemeOf(ref : MetricRef) : String = ref.group.getMetricIdentifier(ref.name)

            override def toString : String = getClass.getName
        }

        case class ScopeDropLeft(limit: Int) extends NameBy {
            override def nemeOf(ref : MetricRef) : String = {
                val all = ref.group.getScopeComponents
                if (all.size < limit) ref.name // warning
                else if (all.size == limit) return ref.name
                else s"${all.drop(limit).mkString(".")}.${ref.name}"
            }
        }

        def apply(config: MetricConfig): NameBy = {

            val limit = config.getInteger("name-by.scope-drop-left", -1)
            if ( limit > 0 ) new ScopeDropLeft(limit)
            else new Origin
        }

    }



}
