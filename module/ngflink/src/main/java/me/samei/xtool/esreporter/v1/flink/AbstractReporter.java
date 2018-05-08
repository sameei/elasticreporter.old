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



    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<Gauge<?>, String> gauges = new HashMap<>();
    protected final Map<Counter, String> counters = new HashMap<>();
    protected final Map<Histogram, String> histograms = new HashMap<>();
    protected final Map<Meter, String> meters = new HashMap<>();

    abstract protected Select select();
    abstract protected String name();

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
            temp.add(fromGauge(item.getValue(), item.getKey()));
        }

        for(Map.Entry<Counter, String> item: counters.entrySet()) {
            temp.add(fromCounter(item.getValue(), item.getKey()));
        }

        for(Map.Entry<Histogram, String> item: histograms.entrySet()) {
            for(Value value: fromHistogram(item.getValue(), item.getKey())) {
                temp.add(value);
            }
        }

        for(Map.Entry<Meter, String> item: meters.entrySet()) {
            for(Value value: fromMeter(item.getValue(), item.getKey())) {
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

    protected  <T> Value fromGauge(String rawKey, Gauge<T> gauge) {

        Value value = null;
        T rawValue = gauge.getValue();

        if (rawValue instanceof java.lang.Number) value = fromNumber(rawKey, (java.lang.Number) rawValue);
        else if (rawValue instanceof java.lang.String) value = fromString(rawKey, (String) rawValue);
        else value = underlay.formatString(rawKey, (String) rawValue);

        return value;
    }

    protected Value fromCounter(String key, Counter counter) {
        return fromNumber(key, counter.getCount());
    }

    protected Collection<Value> fromHistogram(String key, Histogram histogram) {

        ArrayList<Value> list = new java.util.ArrayList<>(8);
        HistogramStatistics stats = histogram.getStatistics();

        list.add(fromNumber(key + "." + "count", histogram.getCount()));
        list.add(fromNumber(key + "." + "min", stats.getMin()));
        list.add(fromNumber(key + "." + "max", stats.getMax()));
        list.add(fromNumber(key + "." + "mean", stats.getMean()));
        list.add(fromNumber(key + "." + "stddev", stats.getStdDev()));
        list.add(fromNumber(key + "." + "75percent", stats.getQuantile(0.75)));
        list.add(fromNumber(key + "." + "95percent", stats.getQuantile(0.95)));
        list.add(fromNumber(key + "." + "99percent", stats.getQuantile(0.99)));

        return list;
    }

    protected Collection<Value> fromMeter(String key, Meter meter) {

        ArrayList<Value> list = new java.util.ArrayList<>(2);

        list.add(fromNumber(key + "." + "count", meter.getCount()));
        list.add(fromNumber(key + "." + "rate", meter.getRate()));

        return list;
    }

}
