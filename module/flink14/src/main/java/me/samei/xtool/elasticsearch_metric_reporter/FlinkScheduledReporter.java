package me.samei.xtool.elasticsearch_metric_reporter;

import me.samei.xjava.util.CollectionFormatter;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.AbstractReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class FlinkScheduledReporter extends AbstractReporter implements Scheduled {

    private Logger logger;

    private FlinkFieldFormatter entryFormatter;
    private DateTimeFieldGenerator dateEntry;
    private IndexGenerator index;
    private ElasticSearchIndexer indexer;

    private final String sep = ",";

    @Override
    public String filterCharacters(String input) {
        return input;
    }

    @Override
    public void open(MetricConfig config) {

        logger = LoggerFactory.getLogger(getClass());

        try {

            String prefix = config.getString("index_prefix", null);
            if (prefix == null) throw new IllegalArgumentException("The 'index_prefix' is mandatory!");

            String duration = config.getString("index_age", null);
            if (duration == null) throw new IllegalArgumentException("The 'index_age' is mandatory!");

            String host = config.getString("es_url", null);
            if (host == null) throw new IllegalArgumentException("The 'es_url' is mandatory!");

            entryFormatter = new FlinkFieldFormatter();
            dateEntry = new DateTimeFieldGenerator.ImplV1();

            if (duration.equals("weekly")) index = IndexGenerator.weekly(prefix);
            else if (duration.equals("daily")) index = IndexGenerator.daily(prefix);
            else throw new IllegalArgumentException("Invalid value for 'index_age': '" + duration +"'; valid values: 'daily', 'weekly'");

            indexer = new ElasticSearchIndexer(host);

            if (logger.isInfoEnabled()) logger.info(
                    "Open, URL: {}, Index: {}", host, index
            );
            if (logger.isDebugEnabled()) logger.debug(
                    "Open, IndexPrefix: {}, Duration: {}, Host: {}, ESIndexer: {}",
                    prefix, duration, host, indexer
            );

        } catch (Exception cause) {
            throw new RuntimeException("Unable to configure ESReporter", cause);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void report() {

        try {
            long now = System.currentTimeMillis();
            String indexName = index.indexForTime(now);
            String json = generateJson(now, indexName);

            indexer.put(indexName, now, json);

            if (logger.isDebugEnabled()) logger.debug("Report, Time: {}, Index: {}", now, indexName);

        } catch (Exception cause) {
            if (logger.isWarnEnabled()) logger.warn("Report, Failure", cause);
        }

    }

    private String generateJson(
            long time,
            String index
    ) {

        StringBuilder buffer = new StringBuilder();

        buffer
                .append("{")
                .append(" \"@meta_generated_at\": ").append(time)
                .append(sep)
                .append(" \"@meta_target_index\": \"").append(index).append("\"")

        ;

        CollectionFormatter formatter = new CollectionFormatter(buffer, sep);

        formatter
                .applyStrings(dateEntry.generate(time, entryFormatter))
                .applyKV(gauges, (Gauge<?> k, String v) -> { return entryFormatter.format(v,k); })
                .applyKV(counters, (Counter k, String v) -> { return entryFormatter.format(v,k); })
                .applyKVs(histograms, (Histogram k, String v) -> { return entryFormatter.format(v,k); })
                .applyKVs(meters, (Meter k, String v) -> { return entryFormatter.format(v,k); })
        ;

        buffer.append("}");

        return buffer.toString();
    }

}
