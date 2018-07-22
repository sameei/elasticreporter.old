package xyz.sigmalab.xtool.elasticreporter.v1.common

trait GroupedMetrics {
    def id: String
    def metrics[C <: ReportContext](context: C): Seq[context.formatter.Val]
    def vars[C <: ReportContext](context: C): Map[String, String]
}
