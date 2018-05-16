package com.sameei.xtool.esreporter.v2

import scala.util.Try

trait Formatter {
    def formatNumber(rawKey: String, rawValue: java.lang.Number): Value
    def formatString(rawKey: String, rawValue: String): Value
}

trait Time {
    def millis : Long
    def readable : String
    def formatter : java.time.format.DateTimeFormatter
}

case class Value(key: String, value: String, `type`: Value.Type, tags: Seq[String])

object Value {
    sealed trait Type
    case object Simple extends Type
    case object Quoted extends Type
}

trait Meta {
    def keyWih(name: String): String // @meta....
}

trait Context extends Formatter with Time with Meta

trait GroupedMetrics {
    def metrics(): Seq[Value]
    def vars(): Map[String, String]
    def collect(context: Context) // Metrics & Vars
}

trait Report

trait Generator {
    def generate(groupedMetrics: GroupedMetrics): Report
}

trait Exporter {
    def export(report: Report): Try[Unit]
    def silently(report: Report): Unit
}

trait ESReporter {}

/*class Reporter(
    es: ElasticSearch,
    indexAndId: IndexAndId,
    formatter: Formatter,
    meta: Meta
) {
    def report()
}*/

class ElasticSearch {}

class IndexAndId {}

