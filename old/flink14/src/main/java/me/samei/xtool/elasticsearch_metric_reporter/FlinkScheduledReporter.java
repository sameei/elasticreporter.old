package me.samei.xtool.elasticsearch_metric_reporter;

import me.samei.xjava.util.CollectionFormatter;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.AbstractReporter;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FlinkScheduledReporter implements MetricReporter, Scheduled, CharacterFilter {


    static public enum Scope {
        JobManager,         // <host>
        JobManagerJob,      // <host>, <job_name>
        TaskManager,
        TaskManagerJob,
        Task, Operator,
        Uncategorized
    }

    static public enum InternalGroup {
        JVMStatus,          // "Status.JVM.*"
        RuntimeState,       // "taskSlotsAvailable|taskSlotsTotal|numRegistereTaskManagers|numRunningJobs"
        NetworkStatus,      // "Status.Network.*"
    }

    protected Scope determine(MetricGroup mg, String name) {
        String id = mg.getMetricIdentifier(name);
        return null;
    }

    static public String[] all = {
            "localhost.jobmanager.Status.JVM.ClassLoader.ClassesLoaded",
            "localhost.jobmanager.Status.JVM.ClassLoader.ClassesUnloaded",
            "localhost.jobmanager.Status.JVM.GarbageCollector.PS Scavenge.Count",
            "localhost.jobmanager.Status.JVM.GarbageCollector.PS Scavenge.Time",
            "localhost.jobmanager.Status.JVM.GarbageCollector.PS MarkSweep.Count",
            "localhost.jobmanager.Status.JVM.GarbageCollector.PS MarkSweep.Time",
            "localhost.jobmanager.Status.JVM.Memory.Heap.Used",
            "localhost.jobmanager.Status.JVM.Memory.Heap.Committed",
            "localhost.jobmanager.Status.JVM.Memory.Heap.Max",
            "localhost.jobmanager.Status.JVM.Memory.NonHeap.Used",
            "localhost.jobmanager.Status.JVM.Memory.NonHeap.Committed",
            "localhost.jobmanager.Status.JVM.Memory.NonHeap.Max",
            "localhost.jobmanager.Status.JVM.Memory.Direct.Count",
            "localhost.jobmanager.Status.JVM.Memory.Direct.MemoryUsed",
            "localhost.jobmanager.Status.JVM.Memory.Direct.TotalCapacity",
            "localhost.jobmanager.Status.JVM.Memory.Mapped.Count",
            "localhost.jobmanager.Status.JVM.Memory.Mapped.MemoryUsed",
            "localhost.jobmanager.Status.JVM.Memory.Mapped.TotalCapacity",
            "localhost.jobmanager.Status.JVM.Threads.Count",
            "localhost.jobmanager.Status.JVM.CPU.Load",
            "localhost.jobmanager.Status.JVM.CPU.Time",
            "localhost.jobmanager.taskSlotsAvailable",
            "localhost.jobmanager.taskSlotsTotal",
            "localhost.jobmanager.numRegisteredTaskManagers",
            "localhost.jobmanager.numRunningJobs",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.ClassLoader.ClassesLoaded",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.ClassLoader.ClassesUnloaded",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.GarbageCollector.PS Scavenge.Count",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.GarbageCollector.PS Scavenge.Time",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.GarbageCollector.PS MarkSweep.Count",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.GarbageCollector.PS MarkSweep.Time",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Heap.Used",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Heap.Committed",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Heap.Max",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.NonHeap.Used",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.NonHeap.Committed",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.NonHeap.Max",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Direct.Count",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Direct.MemoryUsed",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Direct.TotalCapacity",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Mapped.Count",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Mapped.MemoryUsed",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Memory.Mapped.TotalCapacity",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.Threads.Count",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.CPU.Load",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.JVM.CPU.Time",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.Network.TotalMemorySegments",
            "localhost.taskmanager.4794b1a4c645d67a0212290972e09469.Status.Network.AvailableMemorySegments"
    };

    /*

    metrics.scope.jm
        Default: <host>.jobmanager
        Applied to all metrics that were scoped to a job manager.
    metrics.scope.jm.job
        Default: <host>.jobmanager.<job_name>
        Applied to all metrics that were scoped to a job manager and job.
    metrics.scope.tm
        Default: <host>.taskmanager.<tm_id>
        Applied to all metrics that were scoped to a task manager.
    metrics.scope.tm.job
        Default: <host>.taskmanager.<tm_id>.<job_name>
        Applied to all metrics that were scoped to a task manager and job.
    metrics.scope.task
        Default: <host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
        Applied to all metrics that were scoped to a task.
    metrics.scope.operator
        Default: <host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>
        Applied to all metrics that were scoped to an operator.

     */

    private Logger logger;

    protected final Map<Gauge<?>, String> gauges = new HashMap<>();
    protected final Map<Counter, String> counters = new HashMap<>();
    protected final Map<Histogram, String> histograms = new HashMap<>();
    protected final Map<Meter, String> meters = new HashMap<>();

    protected final Map<MetricGroup, Collection<Metric>> inGroups = new HashMap<>();

    private FlinkFieldFormatter entryFormatter;
    private DateTimeFieldGenerator dateEntry;
    private IndexGenerator index;
    private ElasticSearchIndexer indexer;
    private String identity;

    private final String sep = ",";




    @Override
    public String filterCharacters(String input) {
        return input;
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        logger.warn("ADD.METRIC, {}, {}", metric.getClass().getName(), metric);
        logger.warn("ADD.NAME, {}", metricName);
        logger.warn("ADD.GROUP, {}, {}", group.getClass().getName());
        logger.warn("ADD.ID, {}", group.getMetricIdentifier(metricName));
        for(String i: group.getScopeComponents()) { logger.warn("ADD.SCOPE, {}", i); }
        for(Map.Entry<String, String> i: group.getAllVariables().entrySet()) { logger.warn("ADD.VAL, {}, {}", i.getKey(), i.getValue()); }

        final String name = group.getMetricIdentifier(metricName, this);

        synchronized (this) {
            if (metric instanceof Counter) {
                counters.put((Counter) metric, name);
            } else if (metric instanceof Gauge) {
                gauges.put((Gauge<?>) metric, name);
            } else if (metric instanceof Histogram) {
                histograms.put((Histogram) metric, name);
            } else if (metric instanceof Meter) {
                meters.put((Meter) metric, name);
            } else {
                logger.warn("Cannot add unknown metric type {}. This indicates that the reporter " +
                        "does not support this metric type.", metric.getClass().getName());
            }
        }

        logger.warn("");

    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        logger.warn("REMOVE, {}, {}, {}", metric, metricName, group);

        synchronized (this) {
            if (metric instanceof Counter) {
                counters.remove(metric);
            } else if (metric instanceof Gauge) {
                gauges.remove(metric);
            } else if (metric instanceof Histogram) {
                histograms.remove(metric);
            } else if (metric instanceof Meter) {
                meters.remove(metric);
            } else {
                logger.warn("Cannot remove unknown metric type {}. This indicates that the reporter " +
                        "does not support this metric type.", metric.getClass().getName());
            }
        }

    }

    @Override
    public void open(MetricConfig config) {

        logger = LoggerFactory.getLogger(getClass());

        for (Map.Entry<Object, Object> i: config.entrySet()) {
            logger.warn("CONFIG.KEY, {}, {}", i.getKey().getClass().getName(), i.getKey());
            logger.warn("CONFIG.VALUE, {}, {}", i.getValue().getClass().getName(), i.getValue());
        }

        try {

            String prefix = config.getString("index_prefix", null);
            if (prefix == null) throw new IllegalArgumentException("The 'index_prefix' is mandatory!");

            String duration = config.getString("index_age", null);
            if (duration == null) throw new IllegalArgumentException("The 'index_age' is mandatory!");

            String host = config.getString("es_url", null);
            if (host == null) throw new IllegalArgumentException("The 'es_url' is mandatory!");

            entryFormatter = new FlinkFieldFormatter();
            dateEntry = new DateTimeFieldGenerator.ImplV1();

            if (duration.equals("weekly")) index = IndexGenerator.weekly(prefix);
            else if (duration.equals("daily")) index = IndexGenerator.daily(prefix);
            else throw new IllegalArgumentException("Invalid value for 'index_age': '" + duration +"'; valid values: 'daily', 'weekly'");

            indexer = new ElasticSearchIndexer(host);

            if (logger.isInfoEnabled()) logger.info(
                    "Open, URL: {}, Index: {}", host, index
            );
            if (logger.isDebugEnabled()) logger.debug(
                    "Open, IndexPrefix: {}, Duration: {}, Host: {}, ESIndexer: {}",
                    prefix, duration, host, indexer
            );

            identity = config.getString("host_id", null);
            if (identity == null) throw new IllegalAccessException("The 'host_id' is mandatory!");

            logger.info("Open, Host Identity: {}", identity);

        } catch (Exception cause) {
            throw new RuntimeException("Unable to configure ESReporter", cause);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void report() {

        try {
            long now = System.currentTimeMillis();
            String indexName = index.indexForTime(now);
            String json = generateJson(now, indexName);

            indexer.put(indexName, now, json);

            if (logger.isDebugEnabled()) logger.debug("Report, Time: {}, Index: {}", now, indexName);

        } catch (Exception cause) {
            if (logger.isWarnEnabled()) logger.warn("Report, Failure", cause);
        }

    }

    private String generateJson(
            long time,
            String index
    ) throws java.net.UnknownHostException {

        StringBuilder buffer = new StringBuilder();

        buffer
                .append("{")
                .append(" \"@meta_generated_at\": ").append(time)
                .append(sep)
                .append(" \"@meta_target_index\": \"").append(index).append("\"")
                .append(sep)
                .append(" \"@meta_source_host\": \"").append(identity).append("\"")
        ;

        CollectionFormatter formatter = new CollectionFormatter(buffer, sep);

        formatter
                .applyStrings(dateEntry.generate(time, entryFormatter))
                .applyKV(gauges, (Gauge<?> k, String v) -> { return entryFormatter.format(v,k); })
                .applyKV(counters, (Counter k, String v) -> { return entryFormatter.format(v,k); })
                .applyKVs(histograms, (Histogram k, String v) -> { return entryFormatter.format(v,k); })
                .applyKVs(meters, (Meter k, String v) -> { return entryFormatter.format(v,k); })
        ;

        buffer.append("}");  

        InetAddress addr = InetAddress.getLocalHost();
        logger.warn("HostName: {}", addr.getHostName());
        logger.warn("Addr: {}", addr.getHostAddress());
        logger.warn("String: {}", addr.toString());

        return buffer.toString();
    }

}

