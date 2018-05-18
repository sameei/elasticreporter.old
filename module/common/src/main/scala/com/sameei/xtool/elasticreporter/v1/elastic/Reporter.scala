package com.sameei.xtool.elasticreporter.v1.elastic

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import com.sameei.xtool.elasticreporter.v1.{common, elastic}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


class Reporter(name: String, config: Reporter.Config){

    private val logger = LoggerFactory.getLogger(s"${name}")

    logger.debug(s"Init ..., Class: ${getClass.getName}")

    private val es = Elastic(s"${name}.elastic",config.host)

    private val index = new IndexAndId(config.indexPattern, config.idPattern)

    private val zoneId = ZoneId.of(config.zone)

    private val dtf = DateTimeFormatter.ofPattern(config.datetimePattern).withZone(zoneId)

    private def contextAt(time: Long) = new elastic.Reporter.Context(
        s"${name}-${System.currentTimeMillis()}",
        dtf,
        time,
        config.zone,
        config.source
    )

    def apply(gm : common.GroupedMetrics, time: Long): Unit = {

        implicit val context = contextAt(time)

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
        id: String,
        dtf: DateTimeFormatter,
        time: common.data.Millis,
        zone: String,
        source: String
    ) extends common.ReportContextV1(id, dtf, time, "@meta") {

        override val localdatetime : LocalDateTime = {
            val i = java.time.Instant.ofEpochMilli(time).atZone(ZoneId.of(zone))
            LocalDateTime.from(i)
        }

        override def vals : Seq[formatter.Val] = Seq(
            formatter.formatLong(keyFor("time.millis"), time),
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

    object CustomVar {
        val Year = "<year>"
        val Month = "<month>"
        val DayOfMonth = "<day_of_month>"
        val Millis = "<millis>"
        val SourceId = "<source_id>"
    }

    case class Report(index: String, doc: String, body: String)
}
