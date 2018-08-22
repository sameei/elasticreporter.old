package xyz.sigmalab.xtool.elasticreporter.v1.kamon

import com.typesafe.config.Config
import kamon.MetricReporter
import kamon.metric.PeriodSnapshot
import xyz.sigmalab.xtool.elasticreporter.v1.elastic
import org.slf4j.LoggerFactory

object Reporter {

    def fromConfig(config: Config, ns: String): Reporter = {

        val name = config.getString(s"${ns}.name")

        val cfg = elastic.Reporter.Config(
            host = config.getString(s"${ns}.elastic-url"),
            source = config.getString(s"${ns}.source-id"),
            indexPattern = config.getString(s"${ns}.index-pattern"),
            idPattern = config.getString(s"${ns}.id-pattern"),
            datetimePattern = config.getString(s"${ns}.datetime-pattern"),
            zone = config.getString(s"${ns}.datetime-zone")
        )

        val underlay = new elastic.Reporter(
            s"${name}.underlay", cfg,
            new elastic.Reporter.ContextFactory.Default
        )

        new Reporter(name, underlay)
    }

}

class Reporter(
    val name: String,
    protected val underlay: elastic.Reporter
) extends MetricReporter {

    protected val logger = LoggerFactory.getLogger(name)

    override def reportPeriodSnapshot(snapshot : PeriodSnapshot) : Unit = {
        if (logger.isTraceEnabled()) logger.trace(s"Repoter, Time: ${snapshot.to.toEpochMilli}, ...")
        underlay.apply(
            lego.GroupedMetric(name, snapshot.metrics),
            snapshot.to.toEpochMilli
        )
        logger.info(s"Repoter, Time: ${snapshot.to.toEpochMilli}")
    }

    override def start() : Unit = {
        logger.info("Start")
    }

    override def stop() : Unit = {
        logger.info("Stop")
    }

    override def reconfigure(config : Config) : Unit = {
        logger.info("Reconfigure !")
    }
}

