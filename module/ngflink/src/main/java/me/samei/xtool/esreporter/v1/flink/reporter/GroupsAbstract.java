package me.samei.xtool.esreporter.v1.flink.reporter;

import me.samei.xtool.esreporter.v1.flink.util.GroupedMetrics;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.Scheduled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

abstract public class GroupsAbstract extends InitAbstract implements Scheduled {

    protected final Map<String, GroupedMetrics> groups = new HashMap<>();

    protected final ArrayList<GroupedMetrics.Container> removelist = new ArrayList<>();

    protected GroupedMetrics getGroup(String id) {
        return groups.getOrDefault(id,null);
    }

    protected GroupedMetrics getGroupOrMakeIt(String id) {
        GroupedMetrics group = getGroup(id);
        if (group == null) {
            group = new GroupedMetrics();
            groups.put(id, group);
            if (logger.isDebugEnabled()) logger.debug("Added Group: {}", id);
        }
        return group;
    }

    protected void removeMetric(String id, String key, Metric metric, String name, MetricGroup metricGroup) {
        GroupedMetrics group = getGroup(id);
        if (group == null) return;
        group.remove(key, metric, name, metricGroup);
        if (group.size() > 0) return;
        groups.remove(id);
        if (logger.isDebugEnabled()) logger.debug("Removed Group: {}", id);
    }

    abstract public String select(Metric metric, String name, MetricGroup group);
    abstract public String identity(Metric metric, String metricName, MetricGroup group);

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        String ref = select(metric, metricName, group);
        if (ref != null) {
            synchronized (this) {
                String id = identity(metric, metricName, group);

                if (logger.isTraceEnabled()) logger.trace(
                        "Add Metric, NAME: {}, ID: {}, VARS: {}, METRIC: {}",
                        ref, id,
                        group.getAllVariables().size(),
                        group.getMetricIdentifier(metricName)
                );

                getGroupOrMakeIt(id).add(ref, metric, metricName, group);
            }
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        synchronized (this) {
            String id = identity(metric, metricName, group);
            removelist.add(new GroupedMetrics.Container(id, metricName, metric, group));

            if (logger.isTraceEnabled()) logger.trace(
                    "Queue to Remove Metric, NAME: {}, ID: {}, VARS: {}, METRIC: {}",
                    metricName, id,
                    group.getAllVariables().size(),
                    group.getMetricIdentifier(metricName)
            );
        }
    }

    @Override
    public void report() {

        long time = System.currentTimeMillis();

        if (logger.isTraceEnabled()) logger.trace("Reporting, Time: {}, Groups: {}", time, groups.size());

        for (Map.Entry<String, GroupedMetrics> item: groups.entrySet()) {

            if (logger.isTraceEnabled()) logger.trace(
                    "Reporting, Time: {}, ID: {}, Group: {}, Vars: {}",
                    time,
                    item.getKey(),
                    item.getValue().size(),
                    item.getValue().allVars().size()
            );

            report(item.getKey(), time, item.getValue());
        }

        if (logger.isTraceEnabled()) logger.trace("GC Metrics, Time: {}, Metrics: {}", time, removelist.size());
        for (GroupedMetrics.Container cntr: removelist) {
            removeMetric(cntr.key, cntr.name, cntr.metric, cntr.name, cntr.group);
        }

        if (logger.isDebugEnabled()) logger.debug("Report, Time: {}, Groups: {}, Removed Metrics: {}", time, groups.size(), removelist.size());

        removelist.clear();
    }

}
