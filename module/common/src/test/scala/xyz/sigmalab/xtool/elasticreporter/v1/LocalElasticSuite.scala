package xyz.sigmalab.xtool.elasticreporter.v1

import xyz.sigmalab.xtool.elasticreporter.v1.elastic.Elastic
import org.scalatest._

class LocalElasticSuite extends FlatSpec  with Matchers {

    it must "" in {
        new Elastic("localtest", "http://localhost:9200")
    }

}
