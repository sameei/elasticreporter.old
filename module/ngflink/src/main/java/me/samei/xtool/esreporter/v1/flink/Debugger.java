package me.samei.xtool.esreporter.v1.flink;

import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Debugger implements MetricReporter, Scheduled {

    Logger logger = null;


    @Override
    public void open(MetricConfig config) {

        String name = config.getString("logger-name", null);

        if (name == null) throw new IllegalArgumentException("'logger-name' is mandatory!");

        logger = LoggerFactory.getLogger(name);

        logger.debug("OPEN");
    }

    @Override
    public void close() {
        logger.debug("CLOSE");
    }

    protected void logChange(String subject, Metric metric, String metricName, MetricGroup group) {
        logger.debug(subject + ", Class: {}", metric.getClass().getName());
        logger.debug(subject + ", Name: {}", metricName);
        logger.debug(subject + ", Identity: {}", group.getMetricIdentifier(metricName));
        for (String i: group.getScopeComponents()) {
            logger.debug(subject + ", ScopeComponent: {}", i);
        }
        for (Map.Entry<String,String> i: group.getAllVariables().entrySet()) {
            logger.debug(subject + ", Variable: {} => {}", i.getKey(), i.getValue());
        }
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        logChange("AddMetric", metric, metricName, group);
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        logChange("RemoveMetric", metric, metricName, group);
    }

    @Override
    public void report() {
        logger.debug("REPORT");
    }
}
