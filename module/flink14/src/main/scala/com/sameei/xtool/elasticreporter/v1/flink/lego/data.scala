package com.sameei.xtool.elasticreporter.v1.flink.lego

object data {

    trait BaseException extends com.sameei.xtool.elasticreporter.v1.common.data.BaseException

    case class ConfigException(
        desc: String,
        key: String
    ) extends RuntimeException(desc)

    case class InitException(
        desc: String,
        cause: Option[Throwable]
    ) extends RuntimeException(desc, cause.orNull)


}
