package me.samei.xtool.esreporter.v1.flink.reporter;

import me.samei.xtool.esreporter.v1.flink.util.GroupedMetrics;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.Scheduled;

import java.util.HashMap;
import java.util.Map;

abstract public class GroupsAbstract extends InitAbstract implements Scheduled {

    protected final Map<String, GroupedMetrics> groups = new HashMap<>();

    protected GroupedMetrics getGroup(String id) {
        return groups.getOrDefault(id,null);
    }

    protected GroupedMetrics getGroupOrMakeIt(String id) {
        GroupedMetrics group = getGroup(id);
        if (group == null) {
            group = new GroupedMetrics();
            groups.put(id, group);
            logger.info("New Group: {}", id);
        }
        return group;
    }

    protected void removeMetric(String id, String key, Metric metric, String name, MetricGroup metricGroup) {
        GroupedMetrics group = getGroup(id);
        if (group == null) return;
        group.remove(key, metric, name, metricGroup);
        if (group.size() > 0) return;
        groups.remove(id);
    }

    abstract public String select(Metric metric, String name, MetricGroup group);
    abstract public String identity(Metric metric, String metricName, MetricGroup group);

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        String ref = select(metric, metricName, group);
        if (ref != null) {
            synchronized (this) {
                String id = identity(metric, metricName, group);
                logger.info("NAME: {}, ID: {}, VARS: {}, METRIC: {}", ref, id, group.getAllVariables().size(), group.getMetricIdentifier(metricName));
                getGroupOrMakeIt(id).add(ref, metric, metricName, group);
            }
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        synchronized (this) {
            String id = identity(metric, metricName, group);
            removeMetric(id, metricName, metric, metricName, group);
        }
    }

    @Override
    public void report() {
        long time = System.currentTimeMillis();

        for (Map.Entry<String, GroupedMetrics> item: groups.entrySet()) {
            logger.debug("Report: {}, Time: {}", item.getKey(), time);
            report(item.getKey(), time, item.getValue());
        }
    }

}
