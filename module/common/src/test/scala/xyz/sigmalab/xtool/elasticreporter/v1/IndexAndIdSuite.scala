package xyz.sigmalab.xtool.elasticreporter.v1

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

    it must "replace vars properly #3" in {

        // https://stackoverflow.com/questions/11913709/why-does-replaceall-fail-with-illegal-group-reference/20002683#20002683
        // https://stackoverflow.com/questions/9658701/scala-regex-replaceallin-cant-replace-when-replace-string-looks-like-a-regex

        val index = new elastic.IndexAndId("this-is-<key>", "?")

        val value = "CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum)"

        val vars = Map(
            "<key>" -> value
        )

        val rsl = index.index(vars)
        info(rsl)

        index.invalidChars.foreach { ch =>
            rsl.find { _ == ch }.isEmpty shouldBe true
        }
    }

}
