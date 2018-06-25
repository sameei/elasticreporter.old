package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego.data.MetricRef
import org.apache.flink.metrics.MetricConfig
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

import util.WithLogger

trait FilterBy {
    def drop(ref: MetricRef): Boolean
}

object FilterBy {

    @inline def apply(filters: Seq[FilterBy], ref: MetricRef): Option[FilterBy] = {
        if (filters.isEmpty) None
        else filters.find { f => f.drop(ref) }
    }

    case class Nothing(override val name: String) extends FilterBy with WithLogger {

        override def drop(ref : MetricRef) : Boolean = {
            if (logger.isTraceEnabled()) logger.trace(s"Drop: false, ${ref.desc}")
            false
        }
    }

    case class MatchScope(override val name: String, pattern: String) extends FilterBy with WithLogger {

        override def drop(ref : MetricRef) : Boolean = {

            val requiredScope =
                ref.vars.foldLeft(pattern) { case (pt,(k,v)) =>
                    pt.replace(k,v)
                }

            val currentScope = ref.scope.mkString(".")

            val result = ! currentScope.startsWith(requiredScope)

            if (logger.isTraceEnabled()) logger.trace(
                s"Drop: ${result}, RequiredScope: ${requiredScope}, " +
                    s"CurrentScope: ${currentScope}, ${ref.desc}"
            )

            result
        }
    }


    case class ForceVars(override val name: String, pattern: String) extends FilterBy with WithLogger {

        val requireds = {
            val temp = pattern.split('.').toSet
            if (temp.isEmpty) Set(pattern) else temp
        }

        override def drop(ref : MetricRef) : Boolean = {

            val vars = ref.vars

            if (vars.size < requireds.size) {
                if (logger.isTraceEnabled()) logger.trace(
                    s"Drop: true, DiffSize: ${requireds.size} vs. ${vars.size}, " +
                        s"Forced: ${requireds}, ${ref.desc}"
                )
                true
            } else {

                val count = requireds.foldLeft(0) { (count, k) =>
                    if (vars.contains(k)) count + 1
                    else count
                }

                val result = count != requireds.size

                if (logger.isTraceEnabled()) logger.trace(
                    s"Drop: ${result}, DiffSize: ${requireds.size} vs. ${vars.size}, " +
                        s"Forced: ${requireds}, ${ref.desc}"
                )

                result
            }

        }
    }

    case class RejectVars(override val name: String, pattern: String) extends FilterBy with WithLogger {

        val keys = {
            val temp = pattern.split('.').toSet
            if (temp.isEmpty) Set(pattern) else temp
        }

        override def drop(ref : MetricRef) : Boolean = {
            val vars = ref.vars
            val result = keys.find { i => vars.contains(i) }

            if (logger.isTraceEnabled()) logger.trace(
                s"Drop: ${result.isDefined}, RejectedVars: ${result} form ${keys}, ${ref.desc}"
            )

            result.isDefined
        }
    }

    case class SelectScope(override val name: String, scopeName: String) extends FilterBy with WithLogger {

        override def drop (ref : MetricRef) : Boolean = {
            val remainedParts = ref.scope.dropWhile { i => i != scopeName }
            val result = remainedParts.size < 1

            if (logger.isTraceEnabled()) logger.trace(
                s"Drop: ${result}, ScopeName: ${scopeName}, RmainedParts: ${remainedParts.toList}, ${ref.desc}"
            )

            result
        }
    }

    def apply(name: String, config: MetricConfig): Seq[FilterBy] = {

        var filters = ListBuffer.empty[FilterBy]

        Option(config.getString("filter-by.force-vars", null)).foreach { vars =>
            filters += new ForceVars(s"${name}.forcedvars",vars)
        }

        Option(config.getString("filter-by.reject-vars", null)).map { pattern =>
            filters += new RejectVars(s"${name}.rejectvars",pattern)
        }

        Option(config.getString("filter-by.match-scope", null)).map { pattern =>
            filters += new MatchScope(s"${name}.matchscope", pattern)
        }

        Option(config.getString("filter-by.select-scope", null)).map { scopeName =>
            filters += new SelectScope(s"${name}.selectscope", scopeName)
        }

        filters.toList
    }

}
