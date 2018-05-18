package com.sameei.xtool.elasticreporter.v1

import org.scalatest._
import org.scalactic.source.Position

class IndexAndIdSuite extends FlatSpec with Matchers  {

    it must "replace vars properly" in {

        val index = new elastic.IndexAndId("mine-<year>-<month>", "doc-<millis>")

        val vars = Map(
            "<year>" -> 2018.toString,
            "<month>" -> 5.toString,
            "<millis>" -> 121212.toString
        )

        info(index.index(vars))
        index.index(vars) shouldEqual "mine-2018-5"

        info(index.id(vars))
        index.id(vars) shouldEqual "doc-121212"
    }


    it must "replace vars properly #2" in {

        val index = new elastic.IndexAndId("mine-<year>-<month>-<year>", "doc-<millis>")

        val vars = Map(
            "<year>" -> 2018.toString,
            "<month>" -> 5.toString,
            "<millis>" -> 121212.toString
        )

        info(index.index(vars))
        index.index(vars) shouldEqual "mine-2018-5-2018"

    }

}
