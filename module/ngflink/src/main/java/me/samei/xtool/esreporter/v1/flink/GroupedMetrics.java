package me.samei.xtool.esreporter.v1.flink;

import me.samei.xtool.esreporter.v1.common.MetaData;
import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GroupedMetrics {

    private static class Container {
        public final String key;
        public final String name;
        public final Metric metric;
        public final MetricGroup group;
        public Container(String key, String name, Metric metric, MetricGroup group) {
            this.key = key;
            this.name = name;
            this.metric = metric;
            this.group = group;
        }
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Formatter formatter = new Formatter();

    protected final Map<Gauge<?>, Container> gauges = new HashMap<>();
    protected final Map<Counter, Container> counters = new HashMap<>();
    protected final Map<Histogram, Container> histograms = new HashMap<>();
    protected final Map<Meter, Container> meters = new HashMap<>();

    protected final Map<String, String> vars = new HashMap<>();
    protected final Map<String, Integer> varsCount = new HashMap<>();

    protected void addVar(String key, String val) {
        int count = varsCount.getOrDefault(key, 0);
        if (count == 0) { vars.put(key, val); }
        varsCount.put(key, count + 1);
    }

    protected void removeVar(String key) {
        int count = varsCount.getOrDefault(key, 0);
        if (count == 0) {
            varsCount.remove(key);
            vars.remove(key);
        } else {
            varsCount.put(key, count - 1);
        }
    }

    protected void addVars(MetricGroup group) {
        for (Map.Entry<String, String> item: group.getAllVariables().entrySet()) {
            addVar(item.getKey(), item.getValue());
        }
    }

    protected void removeVars(MetricGroup group) {
        for (Map.Entry<String, String> item: group.getAllVariables().entrySet()) {
            removeVar(item.getKey());
        }
    }


    public void add(String key, Metric metric, String name, MetricGroup group) {
        Container cntr = new Container(key, name, metric, group);
        if (metric instanceof Counter) {
            counters.put((Counter) metric, cntr);
        } else if (metric instanceof Gauge) {
            gauges.put((Gauge<?>) metric, cntr);
        } else if (metric instanceof Histogram) {
            histograms.put((Histogram) metric, cntr);
        } else if (metric instanceof Meter) {
            meters.put((Meter) metric, cntr);
        } else {
            logger.warn("Add, Unknown Metric Type: {}, Key: {}", metric.getClass().getName(), key);
        }
    }

    public void remove(String key, Metric metric, String name, MetricGroup group) {
        if (metric instanceof Counter) {
            counters.remove((Counter) metric);
        } else if (metric instanceof Gauge) {
            gauges.remove((Gauge) metric);
        } else if (metric instanceof Histogram) {
            histograms.remove((Histogram) metric);
        } else if (metric instanceof Meter) {
            meters.remove((Meter) metric);
        } else {
            logger.warn("Remove, Unknown Metric Type: {}, Key: {}", metric.getClass().getName(), key);
        }
    }

    protected Collection<Value> collect() {

        ArrayList<Value> temp = new ArrayList<>();

        for(Map.Entry<Gauge<?>, Container> item: gauges.entrySet()) {
            temp.add(formatter.fromGauge(item.getValue().key, item.getKey()));
        }

        for(Map.Entry<Counter, Container> item: counters.entrySet()) {
            temp.add(formatter.fromCounter(item.getValue().key, item.getKey()));
        }

        for(Map.Entry<Histogram, Container> item: histograms.entrySet()) {
            for(Value value: formatter.fromHistogram(item.getValue().key, item.getKey())) {
                temp.add(value);
            }
        }

        for(Map.Entry<Meter, Container> item: meters.entrySet()) {
            for(Value value: formatter.fromMeter(item.getValue().key, item.getKey())) {
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

    protected Collection<Value> collectVars(MetaData meta) {
        ArrayList<Value> list = new ArrayList<>();
        for (Map.Entry<String, String> item: vars.entrySet()) {
            String key = meta.keyWith("var" + "." + item.getKey());
            Value value = formatter.fromString(key, item.getValue());
            list.add(value);
        }
        return list;
    }


}
