package com.sameei.xtool.elasticreporter.v1

import org.scalatest._
import io.circe._
import io.circe.parser._
import org.scalactic.source.Position

import scala.util.{Left, Right}

class FormatterV1Suite extends FlatSpec with Matchers {

    val formatter = new common.FormatterV1

    it must "generate a valid json from empty list" in {

        val json = formatter.format(Nil)

        val parsed = parse(json)

        info(parsed.toString.replace("\n", ""))

        parsed shouldEqual Right(Json.fromJsonObject(JsonObject.empty))
    }

    it must "generate a valid json with a string value" in {

        val key = "@meta.var.<source_id>"
        val value = "localhost"

        val json = formatter.format(
            formatter.formatString(key, value) :: Nil
        )

        val parsed = parse(json)

        info(parsed.toString.replace("\n", ""))

        parsed shouldEqual Right(Json.fromJsonObject(JsonObject(
            key -> Json.fromString(value)
        )))

    }

    it must "generate a valid json with a boolean value" in {

        val key = "are_you_ok"

        def by(value: Boolean)(implicit src: Position) = {

            val json = formatter.format(
                formatter.formatBool(key, value) :: Nil
            )

            val parsed = parse(json)

            info(parsed.toString.replace("\n", ""))

            parsed shouldEqual Right(Json.fromJsonObject(JsonObject(
                key -> Json.fromBoolean(value)
            )))

        }

        by(false)

        by(true)

    }

    it must "generate a valid json with a long value" in {

        val key = "@meta.var.<millis>"
        val value = System.currentTimeMillis()

        val json = formatter.format(
            formatter.formatLong(key, value) :: Nil
        )

        val parsed = parse(json)

        info(parsed.toString.replace("\n", ""))

        parsed shouldEqual Right(Json.fromJsonObject(JsonObject(
            key -> Json.fromLong(value)
        )))

    }

    it must "generate a valid json with a float value" in {

        val key = "@meta.var.<fail_rate>"
        val value = 0.03f

        val json = formatter.format(
            formatter.formatFloat(key, value) :: Nil
        )

        val parsed = parse(json)

        info(parsed.toString.replace("\n", ""))

        parsed shouldEqual Right(Json.fromJsonObject(JsonObject(
            key -> Json.fromFloat(value).get
        )))

    }

    it must "generate a valid list with multiple values" in {

        val json = formatter.format(
            formatter.formatString("name", "Reza") ::
            formatter.formatInt("age", 29) :: Nil
        )

        val parsed = parse(json)

        info(parsed.toString.replace("\n", ""))

        parsed.isRight shouldBe true
    }

}
