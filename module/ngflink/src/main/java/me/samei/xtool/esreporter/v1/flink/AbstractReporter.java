package me.samei.xtool.esreporter.v1.flink;


import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractReporter extends ReporterInitializer implements Scheduled {

    abstract protected GroupedMetrics metrics();
    abstract protected Select select();
    abstract protected String name();

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        String ref = select().apply(metric, metricName, group);
        if (ref != null) {
            synchronized (this) {
                metrics().add(ref, metric, metricName, group);
            }
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        synchronized (this) {
            metrics().remove(null, metric, metricName, group);
        }
    }

    @Override
    public void report() {

        long time = System.currentTimeMillis();

        Collection<Value> values = metrics().collect();

        try {
            underlay.report(time, values);
        } catch (Throwable cause) {
            logger.warn("Failure, Time: " + time, cause);
        }
    }
}
