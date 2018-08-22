package xyz.sigmalab.xtool.elasticreporter.v1.elastic

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import org.slf4j.LoggerFactory
import xyz.sigmalab.xtool.elasticreporter.v1.common

class Generator(
    name: String,
    config: Reporter.Config,
    factory: Reporter.ContextFactory
) extends common.Generator[Reporter.Report] {

    private val logger = LoggerFactory.getLogger(name)

    logger.debug(s"Init ..., Class: ${getClass.getName}")

    val index = new IndexAndId(config.indexPattern, config.idPattern)

    val zoneId = ZoneId.of(config.zone)

    val dtf = DateTimeFormatter.ofPattern(config.datetimePattern).withZone(zoneId)

    def apply(gm : common.GroupedMetrics, time: Long): Reporter.Report = {
        applyWithContext(gm, time, factory.apply(name, time, config))
    }

    def applyWithContext(gm: common.GroupedMetrics, time: Long, context: common.ReportContext): Reporter.Report = {

        if (logger.isTraceEnabled())
            logger.trace(s"Generate ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}")

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
            logger.trace(s"Generate ..., GroupedMetrics: ${gm.id}, Context: ${context.id}, DateTime: ${context.localDateTimeAsString}, Values(${values.size}): ${values}, Vars(${vars.size}): ${vars}")

        val report = Reporter.Report(
            index.index(vars),
            index.id(vars),
            context.formatter.format(values),
            context.meta
        )

        report

    }

}
