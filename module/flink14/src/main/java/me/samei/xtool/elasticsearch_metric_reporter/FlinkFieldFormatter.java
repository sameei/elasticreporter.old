package me.samei.xtool.elasticsearch_metric_reporter;

import org.apache.flink.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.ArrayList;


public class FlinkFieldFormatter
        extends me.samei.xtool.elasticsearch_metric_reporter.AbstractFieldFormatter {

    private Logger logger = LoggerFactory.getLogger(getClass());


    public <T> String format(String key, Gauge<T> gauge) {

        logger.warn("GAUGE: " + key);

        String formattedValue = null;
        T value = gauge.getValue();

        if (value instanceof java.lang.Number) formattedValue = ((Number) value).toString();
        else if (value instanceof java.lang.String) formattedValue = formatValue((String) value);
        else formattedValue = formatValue(value.toString());

        return apply(key, formattedValue);
    }

    public String format(String key, Counter counter) {

        logger.warn("COUNTER: " + key);

        return apply(key, Long.toString(counter.getCount()));
    }


    public Collection<String> format(String key, Histogram histogram) {

        logger.warn("HISTOGRAM: " + key);

        ArrayList<String> list = new java.util.ArrayList<>(8);
        HistogramStatistics stats = histogram.getStatistics();
        String formattedKey = formatKey(key);

        list.add(apply(formattedKey, "count", histogram.getCount()));
        list.add(apply(formattedKey, "min", stats.getMin()));
        list.add(apply(formattedKey, "max", stats.getMax()));
        list.add(apply(formattedKey, "mean", stats.getMean()));
        list.add(apply(formattedKey, "stddev", stats.getStdDev()));
        list.add(apply(formattedKey, "75percent", stats.getQuantile(0.75)));
        list.add(apply(formattedKey, "95percent", stats.getQuantile(0.95)));
        list.add(apply(formattedKey, "99percent", stats.getQuantile(0.99)));

        return list;
    }

    public Collection<String> format(String key, Meter meter) {

        logger.warn("METER: " + key);

        ArrayList<String> list = new java.util.ArrayList<>(2);
        String formattedKey = formatKey(key);

        list.add(apply(formattedKey, "count", meter.getCount()));
        list.add(apply(formattedKey, "min", meter.getRate()));

        return list;
    }

}
