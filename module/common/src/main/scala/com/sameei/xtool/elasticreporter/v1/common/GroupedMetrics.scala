package com.sameei.xtool.elasticreporter.v1.common

trait GroupedMetrics {
    def id: String
    def metrics[V <: Value, F <: Formatter[V], C <: ReportContext[V,F]](context: C): Seq[V]
    def vars[V <: Value, F <: Formatter[V], C <: ReportContext[V,F]](context: C): Map[String, String]
}
