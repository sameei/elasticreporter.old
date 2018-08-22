package xyz.sigmalab.xtool.elasticreporter.v1

import org.scalatest.{FlatSpec, MustMatchers}
import xyz.sigmalab.xtool.elasticreporter.v1.elastic.{Generator, Reporter}

class ContextAndGeneratorSuite extends FlatSpec with MustMatchers {

    val name = "name"

    val config = Reporter.Config(
        host = "http://fake.host:9200",
        source = "test",
        indexPattern = "test-metrics-<year>-<month>",
        idPattern = "<millis>-<uuid>",
        datetimePattern = "yyyy-MM-dd HH:mm:ss",
        zone = "UTC"
    )

    val factory = new Reporter.ContextFactory.Default()

    val generator = new Generator(name, config, factory)

    val contextAtZero = factory.apply(name, 0, config)


    it must "Generat correct index & doc" in {
        val index = generator.index.index(contextAtZero.vars)
        info(index)
        index mustEqual "test-metrics-1970-1"
        val id = generator.index.id(contextAtZero.vars)
        info(id)
        id mustEqual s"0-${contextAtZero.uuid}"
    }

}
