package xyz.sigmalab.xtool.elasticreporter.v1.flink.lego

import xyz.sigmalab.xtool.elasticreporter.v1.flink.lego.util.WithLogger
import org.apache.flink.metrics.MetricConfig
import data._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

trait GroupBy { def groupOf(ref: FlinkMetricRef): String }

object GroupBy {

    case class Nothing(override val name: String) extends GroupBy with WithLogger{
        override def groupOf(ref : FlinkMetricRef) : String = "THE_SINGLE_GROUP"
    }

    case class Pattern(override val name: String, pattern: String) extends GroupBy with WithLogger {

        override def groupOf(ref : FlinkMetricRef) : String = {
            val finalResult = ref.group.getAllVariables.asScala
                .foldLeft(pattern) { case (pt,(k,v)) =>
                    pt.replace(k,v)
                }

            if (logger.isTraceEnabled()) logger.trace(
                s"Group: ${finalResult}, Pattern: ${pattern}, ${ref.desc}"
            )

            finalResult
        }
    }

    def apply(name: String, config: MetricConfig): GroupBy = {
        Option(config.getString("group-by", null)).map { pattern =>
            new Pattern(s"${name}.pattern", pattern)
        } getOrElse { new Nothing(s"${name}.nothing") }
    }
}
