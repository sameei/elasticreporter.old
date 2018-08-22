package xyz.sigmalab.xtool.elasticreporter.v1.flink.lego

import xyz.sigmalab.xtool.elasticreporter.v1.common
import org.apache.flink.metrics._
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

trait Formatters {

    protected def logger: Logger

    def formatCounter[F <: common.Formatter](key: String, counter: Counter)(implicit underlay: F) : underlay.Val = {
        underlay.formatLong(key, counter.getCount)
    }



    def formatGauge[F <: common.Formatter](key: String, gauge: Gauge[_])(implicit underlay: F) : Seq[underlay.Val] = {

        gauge.getValue match {
            case long : java.lang.Long => underlay.formatLong(key, long) :: Nil
            case int : java.lang.Integer => underlay.formatInt(key, int) :: Nil
            case float : java.lang.Float => underlay.formatFloat(key, float) :: Nil
            case double : java.lang.Double => underlay.formatDouble(key, double) :: Nil
            case small : java.lang.Number => underlay.formatInt(key, small.intValue()) :: Nil
            case str : java.lang.String => underlay.formatString(key, str) :: Nil
            case bool : java.lang.Boolean => underlay.formatBool(key, bool) :: Nil
            case bool : Boolean => underlay.formatBool(key, bool) :: Nil


            case hashmap: java.util.Map[_,_] =>

                /*
                from =>
                    LatencyGauge implements Gauge<Map<String, HashMap<String, Double>>>
                    https://github.com/apache/flink/commit/a612b9966f3ee020a5721ac2f039a3633c40146c

                till =>
                    LatencyStats
                    https://github.com/apache/flink/commit/e40cb34868e2c6ff7548653e1e5e2dfbe7d47967
                */

                try {

                    hashmap.asScala.flatMap {
                        case (l1: String, vs: java.util.Map[_,_]) =>
                            vs.asScala.map {
                                case (l2: String, value: Double) =>
                                    underlay.formatDouble(s"${l1}.${l2}", value)
                            }
                    }.toSeq

                } catch {
                    case NonFatal(cause) =>
                        logger.warn(s"Unable to format value: ${cause.getMessage}", cause)
                        dump("Format, Gauge", hashmap)
                        Nil
                }

            case obj =>
                dump("Format, Gauge", obj)
                logger.warn(s"Format, Gauge, ${obj.getClass.getName}, ${obj}")
                Nil
        }
    }

    def formatHistogram[F <: common.Formatter](key: String, histogram: Histogram)(implicit underlay: F): Seq[underlay.Val] = {
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

    def formatMeter[F <: common.Formatter](key: String, meter: Meter)(implicit underlay: F): Seq[underlay.Val] = {
        Seq(
            underlay.formatLong(s"${key}.count", meter.getCount),
            underlay.formatDouble(s"${key}.rate", meter.getRate)
        )
    }

    // =================

    private def formatX[F <: common.Formatter](value: (Any,Any))(implicit underlay: F): Option[underlay.Val] = value match {
        case (k: String, v: java.lang.Float) => Some(underlay.formatFloat(k,v))
        case (k: String, v: java.lang.Double) => Some(underlay.formatDouble(k,v))
        case (k: String, v: String) => Some(underlay.formatString(k,v))
        case (k: String, v: java.lang.Long) => Some(underlay.formatLong(k,v))
        case (k: String, v: java.lang.Integer) => Some(underlay.formatInt(k,v))
        case (k: String, v: java.lang.Number) => Some(underlay.formatInt(k,v.intValue()))
        case (k: String, v: java.lang.Boolean) => Some(underlay.formatBool(k,v))
    }

    private def dump(subject: String, value: Any) = {


        if (value == null) logger.warn(s"Value is null, Subject: ${subject}")
        else logger.warn(s"${subject}, ${value.getClass.getName}:")

        value match {
            case value: java.util.Map[_,_] =>
                value.asScala.foreach { case (k,v) =>
                    logger.warn(s"${subject}, Key(${k.getClass.getName}): ${k}, Value(${v.getClass.getName}): ${v}")
                }
            case value: java.util.Iterator[_] =>
                value.asScala.foreach { i =>
                    logger.warn(s"${subject}, Item(${i.getClass.getName}): ${i}")
                }
            case value: scala.collection.Traversable[_] =>
                value.foreach { i =>
                    logger.warn(s"${subject}, Item(${i.getClass.getName}): ${i}")
                }
            case _ =>
        }

    }

}
