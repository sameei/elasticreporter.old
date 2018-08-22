package xyz.sigmalab.xtool.elasticreporter.v1.common

trait Generator[R <: Report] {
    def apply(gm: GroupedMetrics, time: Long) : R
}
