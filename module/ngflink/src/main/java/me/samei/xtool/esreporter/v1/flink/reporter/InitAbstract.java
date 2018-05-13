package me.samei.xtool.esreporter.v1.flink.reporter;

import me.samei.xtool.esreporter.v1.common.*;
import me.samei.xtool.esreporter.v1.flink.util.Formatter;
import me.samei.xtool.esreporter.v1.flink.util.GroupedMetrics;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public abstract class InitAbstract implements MetricReporter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String sourceId;

    protected Formatter formatter;

    protected IndexName indexName;

    protected MetaData metadata;

    protected ElasticSearch es;

    @Override
    public void open(MetricConfig config) {

        sourceId = config.getString("source-id", null);
        if (sourceId == null) throw new IllegalArgumentException("'source-id' is mandatory!");
        logger.warn("SOURCE: {}", sourceId);

        String url = config.getString("es-url", null);
        if (url == null) throw new IllegalArgumentException("'es-url' is mandatory!");
        logger.warn("URL: {}", url);

        String indexPattern = config.getString("index-pattern", null);
        if (indexPattern== null) throw new IllegalArgumentException("'index-pattern' is mandatory!");
        logger.warn("INDEX: {}", indexPattern);

        String identityPattern = config.getString("identity-pattern", null);
        if (identityPattern == null) throw new IllegalArgumentException("'identity-pattern' is mandatory!");
        logger.warn("IDENTITY: {}", identityPattern);

        String datetimePattern = config.getString("datetime-pattern", null);
        if (datetimePattern== null) throw new IllegalArgumentException("'datetime-pattern' is mandatory!");
        logger.warn("DATETIME: {}", datetimePattern);

        String zone = config.getString("datetime-zone", null);
        if (zone== null) throw new IllegalArgumentException("'datetime-zone' is mandatory!");
        logger.warn("zone: {}", zone);

        try {
            formatter = new Formatter();
            indexName = new IndexName(indexPattern, identityPattern, zone);
            metadata = MetaData.defaultInstance(datetimePattern, zone);
            es = new ElasticSearch(url);
            logger.warn("OPEN");
        } catch(Throwable cause)  {
            logger.warn("FAILURE");
            throw new RuntimeException("Can't cofnig!", cause);
        }
    }

    public void report(String subject, long time, Collection<Value> values, Map<String, String> vars) {
        try {
            logger.warn("VARS: {}", vars.size());
            for(Map.Entry<String, String> i: vars.entrySet()) {
                logger.warn("VAR: {} = {}", i.getKey(), i.getValue());
            }
            Report report = Generator.apply(
                    sourceId, indexName, metadata, formatter,
                    time, values, vars
            );
            es.put(report);
        } catch (Throwable cause) {
            logger.warn("Failure, Time: " + time + ", Subject: " + subject, cause);
        }
    }

    public void report(String subject, long time, GroupedMetrics metrics) {
        report(subject, time, metrics.collect(formatter), metrics.allVars());
    }

    @Override
    public void close() {

    }

}
