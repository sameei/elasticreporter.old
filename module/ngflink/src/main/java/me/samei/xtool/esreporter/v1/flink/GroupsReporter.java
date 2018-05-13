package me.samei.xtool.esreporter.v1.flink;

import me.samei.xtool.esreporter.v1.common.Report;
import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract public class GroupsReporter extends ReporterInitializer implements Scheduled {

    protected final Map<String, GroupedMetrics> groups = new HashMap<>();

    protected GroupedMetrics metric(String id) {
        GroupedMetrics group = groups.getOrDefault(id,null);
        if (group == null) {
            group = new GroupedMetrics();
            groups.put(id, group);
        }
        return group;
    }

    abstract protected Select selector();
    abstract public String groupId(Metric metric, String metricName, MetricGroup group);

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        String ref = selector().apply(metric, metricName, group);
        if (ref != null) {
            synchronized (this) {
                String id = groupId(metric, metricName, group);
                metric(id).add(ref, metric, metricName, group);
            }
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        synchronized (this) {
            String id = groupId(metric, metricName, group);
            GroupedMetrics g = metric(id);
            g.remove(null, metric, metricName, group);
            if (g.size() == 0) groups.remove(id);
        }
    }

    @Override
    public void report() {

        long time = System.currentTimeMillis();

        ArrayList<Report> reports = new ArrayList<>(groups.size());

        for (Map.Entry<String, GroupedMetrics> item: groups.entrySet()) {
            try {
                underlay.report(time, item.getValue().collect(), item.getValue().allVars());
            } catch (Throwable cause) {
                logger.warn("Failure, Time: " + time + ", ID: " + item.getKey(), cause);
            }
        }
    }



}
