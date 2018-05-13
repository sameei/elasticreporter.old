package me.samei.xtool.esreporter.v1.flink.util;

import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.*;

import java.util.ArrayList;
import java.util.Collection;

public class Formatter extends me.samei.xtool.esreporter.v1.common.Formatter.Default {

    public Value fromString(String rawKey, String rawVal) { return formatString(rawKey, rawVal); }

    public Value fromNumber(String rawKey, Number rawVal) { return formatNum(rawKey, rawVal); }

    public  <T> Value fromGauge(String rawKey, Gauge<T> gauge) {

        Value value = null;
        T rawValue = gauge.getValue();

        if (rawValue instanceof java.lang.Number) value = fromNumber(rawKey, (java.lang.Number) rawValue);
        else if (rawValue instanceof java.lang.String) value = fromString(rawKey, (String) rawValue);
        else value = formatString(rawKey, (String) rawValue);

        return value;
    }

    public Value fromCounter(String key, Counter counter) {
        return fromNumber(key, counter.getCount());
    }

    public Collection<Value> fromHistogram(String key, Histogram histogram) {

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

    public Collection<Value> fromMeter(String key, Meter meter) {

        ArrayList<Value> list = new java.util.ArrayList<>(2);

        list.add(fromNumber(key + "." + "count", meter.getCount()));
        list.add(fromNumber(key + "." + "rate", meter.getRate()));

        return list;
    }

}
