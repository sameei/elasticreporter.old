package com.sameei.xtool.elasticreporter.v1.flink.reporter

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.sameei.xtool.elasticreporter.v1.common
import com.sameei.xtool.elasticreporter.v1.elastic.Reporter
import com.sameei.xtool.elasticreporter.v1.flink.{lego, reporter}
import org.apache.flink.metrics._

class JVMStat extends lego.ReporterForSingleGroup {

    val keys = Set(
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
        "Status.JVM.CPU.Time"
    )

    override def select(name : String, metric : Metric, group : MetricGroup) : Option[String] = {
        val id = group.getMetricIdentifier(name)
        keys.find { i => id.endsWith(i) }
    }

    override def contextfactory : Reporter.ContextFactory = new JVMStat.ReportContextFactory("undef")
}

object JVMStat {

    class OnlyJobManager extends JVMStat {
        override def select(name : String, metric : Metric, group : MetricGroup) : Option[String] = {
            val hasJobManager = group.getScopeComponents.find { _ == "jobmanager" }.isDefined
            if (hasJobManager) super.select(name, metric, group) else None
        }

        override def contextfactory = new JVMStat.ReportContextFactory("jobmanager")
    }

    class OnlyTaskManager extends JVMStat {
        override def select(name : String, metric : Metric, group : MetricGroup) : Option[String] = {
            val hasJobManager = group.getScopeComponents.find { _ == "taskmanager" }.isDefined
            if (hasJobManager) super.select(name, metric, group) else None
        }
        override def contextfactory = new JVMStat.ReportContextFactory("taskmanager")
    }

    class ReportContext(name: String, time: Long, config: Reporter.Config, role: String) extends Reporter.Context(
        name, time, config.zone,
        DateTimeFormatter.ofPattern(config.datetimePattern).withZone(ZoneId.of(config.zone)),
        config.source
    ) {

        override def vals = super.vals :+ formatter.formatString(keyFor("source.role"), role)

        override def vars = super.vars + ("<role>" -> role)
    }

    class ReportContextFactory(role: String) extends Reporter.ContextFactory {
        override def apply(namePrefix : String, time : Long, config : Reporter.Config) : common.ReportContext =
            new ReportContext(s"${namePrefix}.JVMStatCustomeContext.${time}", time, config, role)
    }

}
