package me.samei.xtool.esreporter.v1.common

import org.scalatest._
import scala.collection.JavaConverters._
import io.circe._
import io.circe.parser._

class GeneratorSuite extends FlatSpec with Matchers {

    val sourceId = "Nothing"
    val indexPattern = "temp-<year>"
    val datetimePattern = "yyyy-MM"
    val zoneId = "UTC"
    val time = System.currentTimeMillis()

    val underlay = me.samei.xtool.esreporter.v1.common.Reporter.build(
        sourceId,
        "http://localhost:9200",
        indexPattern,
        datetimePattern,
        zoneId
    )

    it must "Generate a value reoprt with empty value list" in {

        val report = underlay.reporter.generate(time, List().asJava, underlay.formatter)

        info(report.toString)

        report.time shouldEqual time

        val parsed = parse(report.body)

        parsed.isRight shouldBe true

        {
            parsed.right.get \\ s"${MetaData.defaultMetaFieldPrefix}.${MetaData.defaultTimeMillisKey}" head
        }.shouldEqual { Json fromLong report.time }
    }

    it must "Generate a valid report with a single simple value" in {

        val report = underlay.reporter.generate(time,List(
            underlay.formatNum("taskSlotsAvailable", 12)
        ).asJava, underlay.formatter)

        info(report.toString)

        val parsed = parse(report.body)

        parsed.isRight shouldBe true

        { parsed.right.get \\ "taskSlotsAvailable" head } shouldEqual Json.fromInt(12)

    }

    it must "Generate a valid report with multiple simple & qouted values" in {

        val report = underlay.reporter.generate(time,List(
            underlay.formatNum("taskSlotsAvailable", 12),
            underlay.formatString("myKey", "Happy")
        ).asJava, underlay.formatter)

        info(report.toString)

        val parsed = parse(report.body)

        parsed.isRight shouldBe true

        { parsed.right.get \\ "taskSlotsAvailable" head } shouldEqual Json.fromInt(12)

        { parsed.right.get \\ "myKey" head } shouldEqual Json.fromString("Happy")

    }
}

