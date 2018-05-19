package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.elastic
import com.sameei.xtool.elasticreporter.v1.elastic.Reporter
import org.apache.flink.metrics.MetricConfig
import org.apache.flink.metrics.reporter.MetricReporter
import org.slf4j.LoggerFactory

trait Open extends MetricReporter { self =>

    protected def name: String

    protected var config: elastic.Reporter.Config = null

    protected var reporter: elastic.Reporter = null

    protected val logger = LoggerFactory getLogger s"${name}.reporter"

    protected val contextfactory: elastic.Reporter.ContextFactory = new Reporter.ContextFactory.Default

    override def open(config : MetricConfig) : Unit = {

        import util._

        self.config = elastic.Reporter.Config(
            host = config.getNonEmptyString("elastic-url"),
            source = config.getNonEmptyString("source-id"),
            indexPattern = config.getNonEmptyString("index-pattern"),
            idPattern = config.getNonEmptyString("id-pattern"),
            datetimePattern = config.getNonEmptyString("datetime-pattern"),
            zone = config.getNonEmptyString("zone")
        )

        self.reporter = new elastic.Reporter(name, self.config, contextfactory)

        logger info s"Open, Config: ${self.config}"
    }
}

/*
metrics.reporter.jvmstat.elastic-url: localhost:9200
metrics.reporter.jvmstat.source-id: single-node
metrics.reporter.jvmstat.index-pattern: jvmstat-<year>-<month>-<day_of_month>
metrics.reporter.jvmstat.id-pattern: jvmstat-<source_id>-<millis>
metrics.reporter.jvmstat.datatime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jvmstat.zone: UTC
*/