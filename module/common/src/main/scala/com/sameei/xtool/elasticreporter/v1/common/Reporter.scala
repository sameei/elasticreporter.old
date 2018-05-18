package com.sameei.xtool.elasticreporter.v1.common

import com.sameei.xtool.elasticreporter.v1.common

trait Reporter[
    Val <: Value,
    Formatter <: common.Formatter[Val],
    Context <: ReportContext[Val, Formatter]
] {

    def apply(gm: GroupedMetrics): Unit

    def applyAll(gms: Seq[GroupedMetrics]): Unit

}
