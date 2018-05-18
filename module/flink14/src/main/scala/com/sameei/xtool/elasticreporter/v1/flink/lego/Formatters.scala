package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.common
import org.apache.flink.metrics._

trait Formatters {

    def formatCounter(key: String, counter: Counter)(implicit underlay: common.Formatter) : underlay.Val = {
        underlay.formatLong(key, counter.getCount)
    }

    def formatGauge(key: String, gauge: Gauge[_])(implicit underlay: common.Formatter) : underlay.Val = {

        gauge.getValue match {
            case long: java.lang.Long => underlay.formatLong(key, long)
            case int: java.lang.Integer => underlay.formatInt(key, int)
            case float: java.lang.Float => underlay.formatFloat(key, float)
            case double: java.lang.Double => underlay.formatDouble(key, double)
            case small: java.lang.Number => underlay.formatInt(key, small.intValue())
            case str: java.lang.String => underlay.formatString(key, str)
            case bool: java.lang.Boolean => underlay.formatBool(key, bool)
            case bool: Boolean => underlay.formatBool(key, bool)

        }
    }

    def formatHistogram(key: String, histogram: Histogram)(implicit underlay: common.Formatter): Seq[underlay.Val] = {
        val stat = histogram.getStatistics
        Seq(
            underlay.formatLong(s"${key}.count", histogram.getCount),
            underlay.formatLong(s"${key}.min", stat.getMin),
            underlay.formatLong(s"${key}.max", stat.getMax),
            underlay.formatDouble(s"${key}.mean", stat.getMean),
            underlay.formatDouble(s"${key}.stddev", stat.getStdDev),
            underlay.formatDouble(s"${key}.in75percent", stat.getQuantile(0.75)),
            underlay.formatDouble(s"${key}.in90percent", stat.getQuantile(0.90)),
            underlay.formatDouble(s"${key}.in95percent", stat.getQuantile(0.95)),
            underlay.formatDouble(s"${key}.in99percent", stat.getQuantile(0.99))
        )
    }

    def formatMeter(key: String, meter: Meter)(implicit underlay: common.Formatter): Seq[underlay.Val] = {
        Seq(
            underlay.formatLong(s"${key}.count", meter.getCount),
            underlay.formatDouble(s"${key}.rate", meter.getRate)
        )
    }

}
