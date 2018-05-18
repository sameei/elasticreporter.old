package com.sameei.xtool.elasticreporter.v1.elastic

import com.sameei.xtool.elasticreporter.v1.common
import common.ReportContextV1
import org.slf4j.LoggerFactory


class Reporter(name: String, config: Reporter.Config){

    private val logger = LoggerFactory.getLogger(s"${name}")

    logger.debug(s"Init ..., Class: ${getClass.getName}")

    private val es = Elastic(s"${name}.elastic",config.host)

    private val index = new IndexAndId(config.indexPattern, config.idPattern)

    private def newContext : common.ReportContextV1 = ???

    def apply(gm : common.GroupedMetrics): Unit = {

        implicit val context = newContext

        if (logger.isTraceEnabled())
            logger.trace(s"Apply ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}")

        val xs = context.vals

        val values = gm.metrics[ReportContextV1.Val, ReportContextV1.Formatter, ReportContextV1.Type](context)

        val vars = gm.vars[ReportContextV1.Val, ReportContextV1.Formatter, ReportContextV1.Type](context)

        if (logger.isTraceEnabled())
            logger.trace(s"Apply ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}, Values(${values.size}): ${values}, Vars(${vars.size}): ${vars}")

        val report = Reporter.Report(
            index.index(vars),
            index.id(vars),
            context.formatter.format(values)
        )

        es.put(report)

    }

}

object Reporter {

    case class Config(
        host: String,
        indexPattern: String,
        idPattern: String,
        zone: String
    )

    case class Report(index: String, doc: String, body: String)
}
