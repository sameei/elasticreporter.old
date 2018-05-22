package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego.data.MetricRef
import org.apache.flink.metrics.MetricConfig
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

trait FilterBy {
    def drop(ref: MetricRef): Boolean
}

object FilterBy {

    @inline def apply(filters: Seq[FilterBy], ref: MetricRef): Option[FilterBy] = {
        if (filters.isEmpty) None
        else filters.find { f => f.drop(ref) }
    }

    class Nothing extends FilterBy {

        override def drop(ref : MetricRef) : Boolean = false

        override def toString : String = getClass.getName
    }

    case class Scope(pattern: String) extends FilterBy {

        override def drop(ref : MetricRef) : Boolean = {

            val requiredScope =
                ref.vars.foldLeft(pattern) { case (pt,(k,v)) =>
                    pt.replace(k,v)
                }

            val currentScope = ref.scope.mkString(".")

            ! currentScope.startsWith(requiredScope)
        }
    }


    case class RequiredVariables(pattern: String) extends FilterBy {

        val requireds = {
            val temp = pattern.split('.').toSet
            if (temp.isEmpty) Set(pattern) else temp
        }

        override def drop(ref : MetricRef) : Boolean = {

            val vars = ref.vars

            if (vars.size < requireds.size) true
            else {

                val count = requireds.foldLeft(0) { (count, k) =>

                    println(vars.contains(k), k, vars.get(k))

                    if (vars.contains(k)) count + 1
                    else count
                }

                count != requireds.size
            }

        }
    }

    case class RejectingVariables(pattern: String) extends FilterBy {

        private val logger = LoggerFactory.getLogger(getClass)

        val keys = {
            val temp = pattern.split('.').toSet
            if (temp.isEmpty) Set(pattern) else temp
        }

        override def drop(ref : MetricRef) : Boolean = {
            val vars = ref.vars
            keys.find { i => vars.contains(i) }.isDefined
        }
    }

    class Status extends FilterBy {

        val postfixes = Array(
            "Status.JVM.ClassLoader.ClassesLoaded",
            "Status.JVM.ClassLoader.ClassesUnloaded",
            "Status.JVM.GarbageCollector.PS Scavenge.Count",
            "Status.JVM.GarbageCollector.PS Scavenge.Time",
            "Status.JVM.GarbageCollector.PS MarkSweep.Count",
            "Status.JVM.GarbageCollector.PS MarkSweep.Time",
            "Status.JVM.Memory.Heap.Used",
            "Status.JVM.Memory.Heap.Committed",
            "Status.JVM.Memory.Heap.Max",
            "Status.JVM.Memory.NonHeap.Used",
            "Status.JVM.Memory.NonHeap.Committed",
            "Status.JVM.Memory.NonHeap.Max",
            "Status.JVM.Memory.Direct.Count",
            "Status.JVM.Memory.Direct.MemoryUsed",
            "Status.JVM.Memory.Direct.TotalCapacity",
            "Status.JVM.Memory.Mapped.Count",
            "Status.JVM.Memory.Mapped.MemoryUsed",
            "Status.JVM.Memory.Mapped.TotalCapacity",
            "Status.JVM.Threads.Count",
            "Status.JVM.CPU.Load",
            "Status.JVM.CPU.Time",
            "Status.Network.AvailableMemorySegments",
            "Status.Network.TotalMemorySegments"
        )

        val others = Array(
            "totalNumberOfCheckpoints",
            "numberOfInProgressCheckpoints",
            "numberOfCompletedCheckpoints",
            "numberOfFailedCheckpoints",
            "lastCheckpointRestoreTimestamp",
            "lastCheckpointSize",
            "lastCheckpointDuration",
            "lastCheckpointAlignmentBuffered",
            "lastCheckpointExternalPath"

        )

        override def drop(ref : MetricRef) : Boolean = {
            val id = ref.id
            postfixes.find { i => id.endsWith(i) }.isEmpty
        }
    }

    def apply(config: MetricConfig): Seq[FilterBy] = {

        var filters = ListBuffer.empty[FilterBy]

        Option(config.getString("filter-by.required-vars", null)).foreach { vars =>
            filters += new RequiredVariables(vars)
        }

        Option(config.getString("filter-by.reject-vars", null)).map { pattern =>
            filters += new RejectingVariables(pattern)
        }

        Option(config.getString("filter-by.scope", null)).map { pattern =>
            filters += new Scope(pattern)
        }

        filters.toList
    }

}
