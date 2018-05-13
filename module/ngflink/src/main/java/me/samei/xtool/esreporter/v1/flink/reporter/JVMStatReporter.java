/*
package me.samei.xtool.esreporter.v1.flink.reporter;

import me.samei.xtool.esreporter.v1.flink.util.GroupedMetrics;
import me.samei.xtool.esreporter.v1.flink.util.Select;

public class JVMStatReporter extends AbstractReporter {

    private static String[] _tokens = {
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
    };

    private GroupedMetrics _metrics = new GroupedMetrics();

    @Override
    public GroupedMetrics metrics() { return _metrics; }

    @Override
    protected String name() { return "JVM"; }


    private Select _select = new Select.MatchByEnd(_tokens, name());

    @Override
    protected Select select() { return _select; }

}
*/
