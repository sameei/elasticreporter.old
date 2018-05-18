package com.sameei.xtool.elasticreporter.v1.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReportContextV1 {
    type Val = FormatterV1.Val
    type Formatter = FormatterV1
    type Type = ReportContext[Val, Formatter]
}

abstract class ReportContextV1(
    override val id : String,
    override val datetimeFormatter : DateTimeFormatter,
    override val time: data.Millis = System.currentTimeMillis(),
    val keyPrefix: String = "@meta"
) extends ReportContextV1.Type {

    override val formatter = new FormatterV1

    override def keyFor(name : String) : String = s"${keyPrefix}.${name}"

    override val localdatetime : LocalDateTime = {
        val i = java.time.Instant.ofEpochMilli(time)
        LocalDateTime.from(i)
    }

    /**
      * Prepared values for this context
      * Every context is able to have some predefined values to let report generators use it!
      * Such as 'time', 'host', ...
      *
      * @return
      */
    /*override def vals : Seq[FormatterV1.Val] = Seq(
        formatter.formatLong(keyFor("time.millis"), time),
        formatter.formatString(keyFor("time.formatted"), localDateTimeAsString)
    )*/

    /**
      * Prepared variables for this context
      *
      * @return
      */
    /*override def vars : Map[String, String] = Map(
        "<year>" -> localdatetime.getYear.toString,
        "<month>" -> localdatetime.getMonth.toString,
        "<day_of_month>" -> localdatetime.getDayOfMonth.toString,
        "<millis>" -> time.toString
    )*/
}
