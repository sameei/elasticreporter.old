package com.sameei.xtool.elasticreporter.v1.flink.lego

import org.apache.flink.metrics.MetricConfig

object util {

    implicit class MetricConfigOps(val underlay: MetricConfig) extends AnyVal {
        def getNonEmptyString(key: String) =
            underlay.getString(key, null) match {
                case null => throw data.ConfigException(s"Undefined Config '${key}'!", key)
                case "" => throw data.ConfigException(s"Empty Value for Cofnig '${key}'!", key)
                case value => value
            }
    }

}
