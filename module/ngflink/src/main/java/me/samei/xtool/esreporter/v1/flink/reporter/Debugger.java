package me.samei.xtool.esreporter.v1.flink.reporter;

import me.samei.xtool.esreporter.v1.common.*;
import me.samei.xtool.esreporter.v1.flink.reporter.InitAbstract;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.slf4j.Logger;

import org.apache.flink.metrics.reporter.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Debugger extends InitAbstract implements Scheduled {

    private HashMap<String, String> empty = new HashMap<String, String>();

    protected Collection<Value> collect(String subject, long time, Metric metric, String name, MetricGroup group) {

        ArrayList<Value> values = new ArrayList<>();

        values.add(fromString("subject", subject));
        values.add(fromString("class", metric.getClass().getName()));
        values.add(fromString("name", name));
        values.add(fromString("identity", group.getMetricIdentifier(name)));

        int i = 1;
        for(String item: group.getScopeComponents()) {
            values.add(fromString("scope." + i, item));
            i++;
        }


        for(Map.Entry<String, String> item: group.getAllVariables().entrySet()) {
            values.add(fromString("var." + item.getKey(), item.getValue()));
        }

        return values;
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        try {
            long now = System.currentTimeMillis();
            Collection<Value> values = collect("AddMetric", now, metric, metricName, group);
            report("AddMetric", now, values, empty);
        } catch (Exception cause) {
            logger.error("ReportFailure: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        try {
            long now = System.currentTimeMillis();
            Collection<Value> values = collect("DeleteMetric", now, metric, metricName, group);
            report("DeleteMetric", now, values, empty);
        } catch (Exception cause) {
            logger.error("ReportFailure: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void report() {}

    public Value fromString(String rawKey, String rawVal) { return formatter.formatString(rawKey, rawVal); }

    public Value fromNumber(String rawKey, Number rawVal) { return formatter.formatNum(rawKey, rawVal); }
}