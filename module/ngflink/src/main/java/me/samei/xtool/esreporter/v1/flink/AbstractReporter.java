package me.samei.xtool.esreporter.v1.flink;


import me.samei.xtool.esreporter.v1.common.Reporter;
import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractReporter implements  Scheduled, MetricReporter {



    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<Gauge<?>, String> gauges = new HashMap<>();
    protected final Map<Counter, String> counters = new HashMap<>();
    protected final Map<Histogram, String> histograms = new HashMap<>();
    protected final Map<Meter, String> meters = new HashMap<>();

    abstract protected Select select();
    abstract protected String name();
    protected Reporter underlay;

    @Override
    public void open(MetricConfig config) {

        String sourceId = config.getString("source-id", null);
        if (sourceId == null) new IllegalArgumentException("'source-id' is mandatory!");

        String url = config.getString("es-url", null);
        if (url == null) new IllegalArgumentException("'es-url' is mandatory!");

        String indexPattern = config.getString("index-pattern", null);
        if (indexPattern== null) new IllegalArgumentException("'index-pattern' is mandatory!");

        String datetimePattern = config.getString("datetime-pattern", null);
        if (datetimePattern== null) new IllegalArgumentException("'datetime-pattern' is mandatory!");

        String zone = config.getString("datetime-zone", null);
        if (zone== null) new IllegalArgumentException("'zone' is mandatory!");

        try {
            underlay = Reporter.build(sourceId, url, indexPattern, datetimePattern, zone);
        } catch(Throwable cause)  {
            throw new RuntimeException("Can't cofnig!", cause);
        }
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        String id = group.getMetricIdentifier(metricName);
        String ref = select().apply(id);
        if (ref != null) {

            String key = ref;

            if (logger.isDebugEnabled()) logger.debug("Name: , Added: '{}', Key: {}", name(), id, key);

            synchronized (this) {
                if (metric instanceof Counter) {
                    counters.put((Counter) metric, key);
                } else if (metric instanceof Gauge) {
                    gauges.put((Gauge<?>) metric, key);
                } else if (metric instanceof Histogram) {
                    histograms.put((Histogram) metric, key);
                } else if (metric instanceof Meter) {
                    meters.put((Meter) metric, key);
                } else {
                    logger.warn("Cannot add unknown metric type {}. This indicates that the reporter " +
                            "does not support this metric type.", metric.getClass().getName());
                }
            }

        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
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
    public void report() {

        long time = System.currentTimeMillis();

        Collection<Value> all = collect();

        try {
            underlay.report(time, all);
        } catch (Throwable cause) {
            logger.warn("Failure, Time: " + time, cause);
        }
    }

    // ===================================================================================

    @Override
    public void close() { }

    // ===================================================================================

    protected Collection<Value> collect() {

        ArrayList<Value> temp = new ArrayList<>();

        for(Map.Entry<Gauge<?>, String> item: gauges.entrySet()) {
            temp.add(format(item.getValue(), item.getKey()));
        }

        for(Map.Entry<Counter, String> item: counters.entrySet()) {
            temp.add(format(item.getValue(), item.getKey()));
        }

        for(Map.Entry<Histogram, String> item: histograms.entrySet()) {
            for(Value value: format(item.getValue(), item.getKey())) {
                temp.add(value);
            }
        }

        for(Map.Entry<Meter, String> item: meters.entrySet()) {
            for(Value value: format(item.getValue(), item.getKey())) {
                temp.add(value);
            }
        }

        HashMap<String, Value> result = new HashMap();
        for(Value v: temp) {
            Value already = result.get(v.key);
            if (already != null) {
                if (already.value.equals(v.value)) {
                    if (logger.isDebugEnabled()) logger.debug("Collect, Duplicated Values, Ignore New One, {}, {}", already, v);
                } else logger.warn("Collect, Duplicated Keys, Ignore New One, {}, {}", already, v);
            } else result.put(v.key, v);
        }

        return result.values();
    }

    public <T> Value format(String rawKey, Gauge<T> gauge) {

        Value value = null;
        T rawValue = gauge.getValue();

        if (rawValue instanceof java.lang.Number) value = underlay.formatNum(rawKey, (java.lang.Number) rawValue);
        else if (rawValue instanceof java.lang.String) value = underlay.formatString(rawKey, (String) rawValue);
        else value = underlay.formatString(rawKey, (String) rawValue);

        return value;
    }

    public Value format(String key, Counter counter) {
        return underlay.formatNum(key, counter.getCount());
    }

    public Collection<Value> format(String key, Histogram histogram) {

        ArrayList<Value> list = new java.util.ArrayList<>(8);
        HistogramStatistics stats = histogram.getStatistics();

        list.add(underlay.formatNum(key + "." + "count", histogram.getCount()));
        list.add(underlay.formatNum(key + "." + "min", stats.getMin()));
        list.add(underlay.formatNum(key + "." + "max", stats.getMax()));
        list.add(underlay.formatNum(key + "." + "mean", stats.getMean()));
        list.add(underlay.formatNum(key + "." + "stddev", stats.getStdDev()));
        list.add(underlay.formatNum(key + "." + "75percent", stats.getQuantile(0.75)));
        list.add(underlay.formatNum(key + "." + "95percent", stats.getQuantile(0.95)));
        list.add(underlay.formatNum(key + "." + "99percent", stats.getQuantile(0.99)));

        return list;
    }

    public Collection<Value> format(String key, Meter meter) {

        ArrayList<Value> list = new java.util.ArrayList<>(2);

        list.add(underlay.formatNum(key + "." + "count", meter.getCount()));
        list.add(underlay.formatNum(key + "." + "rate", meter.getRate()));

        return list;
    }

}
