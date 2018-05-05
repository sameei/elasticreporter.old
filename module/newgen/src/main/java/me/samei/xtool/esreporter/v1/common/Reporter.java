package me.samei.xtool.esreporter.v1.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class Reporter implements Formatter {

    public final ElasticSearch es;
    public final Generator reporter;
    public final Formatter formatter;

    protected final Logger logger;

    public Reporter(
            ElasticSearch es,
            Generator reporter,
            Formatter formatter
    ) {
        this.es = es;
        this.reporter = reporter;
        this.formatter = formatter;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public void report(long time, Collection<Value> values) throws IOException {

        if (logger.isTraceEnabled()) logger.trace("Report, Time: {}, Values({}) ...", time, values.size());

        Report report = reporter.generate(time, values, formatter);

        if (logger.isTraceEnabled()) logger.trace(
                "Report, Time: {}, Index: {}, Body({}) ...",
                report.time, report.index, report.body.length()
        );

        boolean successful = es.put(report);

        if (successful) {
            if (logger.isDebugEnabled()) logger.debug("Report, Time: {}, Index: {}", report.time, report.index);
        } else if (logger.isWarnEnabled()) {
            logger.warn("Report, Time: {}, Index: {}, Failed!", report.time, report.index);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("(es: ").append(es)
                .append(", reporter: ").append(reporter)
                .append(", formatter: ").append(formatter)
                .append(")").toString();
    }

    static public Reporter build(
            String sourceId,
            String url,
            String indexPattern,
            String datetimePattern,
            String zoneId
    ) throws IOException {
        return new Reporter(
                new ElasticSearch(url),
                new Generator(
                        sourceId,
                        new IndexName(indexPattern, zoneId),
                        MetaData.defaultInstance(datetimePattern, zoneId)
                        ),
                new Formatter.Default()
        );
    }

    @Override
    public String formatKey(String key) {
        return formatter.formatKey(key);
    }

    @Override
    public Value formatString(String rawKey, String rawValue) {
        return formatter.formatString(rawKey, rawValue);
    }

    @Override
    public Value formatNum(String rawKey, Number rawValue) {
        return formatter.formatNum(rawKey, rawValue);
    }
}
