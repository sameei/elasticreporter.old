package me.samei.xtool.esreporter.v1.flink;

import me.samei.xtool.esreporter.v1.common.ElasticSearch;
import me.samei.xtool.esreporter.v1.common.MetaData;
import me.samei.xtool.esreporter.v1.common.Report;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.MetricGroup;
import org.slf4j.Logger;

import org.apache.flink.metrics.reporter.Scheduled;
import org.apache.flink.metrics.reporter.MetricReporter;

import java.util.Iterator;
import java.util.Map;

public class Debugger implements Scheduled, MetricReporter {

    private Logger logger;

    private ElasticSearch elastic;

    private String index;

    private static String timeKey = MetaData.defualtMetaFieldPrefix + "." + MetaData.defaultTimeMillisKey;


    @Override
    public void open(MetricConfig config) {


        String host = config.getString("es-url", null);
        if (host == null) throw new IllegalArgumentException("'es-url' is mandatory!");

        String indexPath = config.getString("index", null);
        if (indexPath == null) throw new IllegalArgumentException("'index' is mandatory!");

        try {
            elastic = new ElasticSearch(host);
        } catch (Exception cause) {
            String message = "InitError: " + cause.getMessage();
            logger.error(message, cause);
            throw new RuntimeException(message, cause);
        }
    }

    @Override
    public void close() {

    }

    protected String makeJson(String subject, long time, Metric metric, String name, MetricGroup group) {
        StringBuilder builder = new StringBuilder();

        builder.append("{");

        builder.append("\"subject\": \"").append(subject)
                .append("\", \"class\": \"").append(metric.getClass().getName())
                .append("\", \"name\": \"").append(name)
                .append("\", \"").append(timeKey).append("\": \"").append(time)
                .append("\"");


        builder.append(", \"scopes\": [");
        ArrayIterator iterScopes = new ArrayIterator(group.getScopeComponents());
        if (iterScopes.hasNext()) {
            builder.append('"').append(iterScopes.next()).append('"');
            while(iterScopes.hasNext()) {
                builder.append(',').append(iterScopes.next()).append('"');
            }
        }
        builder.append("]");

        builder.append(", \"\": {");
        Iterator<Map.Entry<String,String>> iterEntries = group.getAllVariables().entrySet().iterator();
        if (iterEntries.hasNext()) {
            Map.Entry<String, String> one = iterEntries.next();
            builder.append('"')
                    .append(one.getKey()).append("\": \"")
                    .append(one.getValue()).append("\"")
            ;
            while(iterEntries.hasNext()) {
                one = iterEntries.next();
                builder.append(',')
                        .append('"').append(one.getKey())
                        .append("\": \"").append(one.getValue())
                        .append("\"");
            }
        }
        builder.append("}");

        return builder.append("}").toString();
    }

    @Override
    public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
        try {
            long now = System.currentTimeMillis();
            String json = makeJson("AddMetric", now, metric, metricName, group);
            Report report = new Report(now, index, json);
            elastic.put(report);
        } catch (Exception cause) {
            logger.error("ReportFailure: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void notifyOfRemovedMetric(Metric metric, String metricName, MetricGroup group) {
        try {
            long now = System.currentTimeMillis();
            String json = makeJson("DeleteMetric", now, metric, metricName, group);
            Report report = new Report(now, index, json);
            elastic.put(report);
        } catch (Exception cause) {
            logger.error("ReportFailure: " + cause.getMessage(), cause);
        }
    }

    @Override
    public void report() {

    }
}
