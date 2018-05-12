package me.samei.xtool.esreporter.v1.flink;

import me.samei.xtool.esreporter.v1.common.Reporter;
import me.samei.xtool.esreporter.v1.common.Value;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public abstract class ReporterInitializer implements MetricReporter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
    public void close() {

    }

}
