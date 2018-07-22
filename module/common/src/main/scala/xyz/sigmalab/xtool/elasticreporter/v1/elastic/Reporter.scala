package xyz.sigmalab.xtool.elasticreporter.v1.elastic

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.UUID

import xyz.sigmalab.xtool.elasticreporter.v1.common.{FormatterV1, data}
import xyz.sigmalab.xtool.elasticreporter.v1.{common, elastic}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


class Reporter(name: String, config: Reporter.Config, factory: Reporter.ContextFactory){

    private val logger = LoggerFactory.getLogger(name)

    logger.debug(s"Init ..., Class: ${getClass.getName}")

    private val es = Elastic(s"${name}.elastic",config.host)

    private val index = new IndexAndId(config.indexPattern, config.idPattern)

    private val zoneId = ZoneId.of(config.zone)

    private val dtf = DateTimeFormatter.ofPattern(config.datetimePattern).withZone(zoneId)

    def apply(gm : common.GroupedMetrics, time: Long): Unit = {

        implicit val context = factory.apply(name, time, config)

        if (logger.isTraceEnabled())
            logger.trace(s"Apply ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}")

        val vars = {
            context.vars ++ gm.vars(context)
        }

        val values = {

            context.vals ++
                gm.metrics(context) ++
                vars.map { case (k,v) =>
                    context.formatter.formatString(context.keyFor(s"var.${k}"), v)
                }
        }

        if (logger.isTraceEnabled())
            logger.trace(s"Apply ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}, Values(${values.size}): ${values}, Vars(${vars.size}): ${vars}")

        val report = Reporter.Report(
            index.index(vars),
            index.id(vars),
            context.formatter.format(values)
        )

        es.put(report) match {

            case Success(_) =>
                if (logger.isInfoEnabled())
                    logger.info(s"Apply, Done, GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}, Index: ${report.index}, Doc: ${report.doc}")

            case Failure(cause) =>
                if (logger.isWarnEnabled())
                    logger.warn(s"Apply, Failed, GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}, Index: ${report.index}, Doc: ${report.doc}, Failure: ${cause.getMessage}", cause)
        }
    }

    def applyAll(gms: Seq[common.GroupedMetrics], time: Long) : Unit = {
        gms.foreach { gm => apply(gm, time) }
    }

}

object Reporter {

    case class Config(
        host: String,
        source: String,
        indexPattern: String,
        idPattern: String,
        datetimePattern: String,
        zone: String
    )

    class Context(
        override val id : String,
        override val time: data.Millis,
        override val zone: String,
        override val datetimeFormatter : DateTimeFormatter,
        source: String,
        val keyPrefix: String = "@meta"
    ) extends common.ReportContext { self =>

        override type Formatter = FormatterV1

        override val formatter = new FormatterV1

        override def keyFor(name : String) : String = s"${keyPrefix}.${name}"

        override val localdatetime : LocalDateTime = {
            val i = java.time.Instant.ofEpochMilli(time).atZone(ZoneId.of(zone))
            LocalDateTime.from(i)
        }

        override def vals : Seq[formatter.Val] = Seq(
            formatter.formatLong(keyFor("time.millis"), time),
            formatter.formatString(keyFor("uuid"), UUID.randomUUID().toString),
            formatter.formatString(keyFor("time.formatted"), localDateTimeAsString),
            formatter.formatString(keyFor("source.id"), source)
        )

        override def vars : Map[String, String] = Map(
            CustomVar.Year -> localdatetime.getYear.toString,
            CustomVar.Month -> localdatetime.getMonthValue.toString,
            CustomVar.DayOfMonth -> localdatetime.getDayOfMonth.toString,
            CustomVar.Millis -> time.toString,
            CustomVar.SourceId -> source
        )
    }

    trait ContextFactory extends ((String, Long, Config) => common.ReportContext) {
        def apply(namePrefix: String, time: Long, config: Config): common.ReportContext
    }

    object ContextFactory {
        class Default extends ContextFactory {
            def apply(namePrefix: String, time: Long, config: Config) =
                new Context(
                    s"${namePrefix}.context.${time}", time, config.zone,
                    DateTimeFormatter.ofPattern(config.datetimePattern).withZone(ZoneId.of(config.zone)),
                    config.source
                )
        }
    }

    object CustomVar {
        val Year = "<year>"
        val Month = "<month>"
        val DayOfMonth = "<day_of_month>"
        val Millis = "<millis>"
        val SourceId = "<source_id>"
    }

    case class Report(index: String, doc: String, body: String)
}
