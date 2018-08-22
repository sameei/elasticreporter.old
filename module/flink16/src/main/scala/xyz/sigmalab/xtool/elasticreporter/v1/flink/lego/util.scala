package xyz.sigmalab.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.MetricConfig
import org.slf4j.LoggerFactory

object util {

    trait WithLogger {
        protected def name: String
        val logger = LoggerFactory.getLogger(name)
    }

    implicit class MetricConfigOps(val underlay: MetricConfig) extends AnyVal {
        def getNonEmptyString(key: String) =
            underlay.getString(key, null) match {
                case null => throw data.ConfigException(s"Undefined Config '${key}'!", key)
                case "" => throw data.ConfigException(s"Empty Value for Cofnig '${key}'!", key)
                case value => value
            }
    }

}
