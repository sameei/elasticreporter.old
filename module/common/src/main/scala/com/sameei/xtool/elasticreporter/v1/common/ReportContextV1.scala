package com.sameei.xtool.elasticreporter.v1.common

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

abstract class ReportContextV1(
    override val id : String,
    override val datetimeFormatter : DateTimeFormatter,
    override val time: data.Millis = System.currentTimeMillis(),
    val keyPrefix: String = "@meta"
) extends ReportContext {

    override type Formatter = FormatterV1

    override val formatter = new FormatterV1

    override def keyFor(name : String) : String = s"${keyPrefix}.${name}"

    override val localdatetime : LocalDateTime = {
        val i = java.time.Instant.ofEpochMilli(time).atZone(ZoneId.of(zone))
        LocalDateTime.from(i)
    }
}
