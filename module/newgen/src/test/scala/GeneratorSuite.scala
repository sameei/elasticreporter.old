
import org.scalatest._
import scala.collection.JavaConverters._

class GeneratorSuite extends FlatSpec with Matchers {

    val underlay = me.samei.xtool.esreporter.v1.common.Reporter.build(
        "nothing",
        "http://localhost:9200",
        "temp-<year>",
        "yyyy-MM",
        "UTC"
    )

    it must "X" in {

        val time = System.currentTimeMillis()

        {
            val report = underlay.reporter.generate(time,List().asJava, underlay.formatter)
            info(report.toString)
        }

        {
            val report = underlay.reporter.generate(time,List(
                underlay.formatNum("taskSlotsAvailable", 12)
            ).asJava, underlay.formatter)
            info(report.toString)
        }

        {
            val report = underlay.reporter.generate(time,List(
                underlay.formatNum("taskSlotsAvailable", 12),
                underlay.formatString("@meta.time.format", "Happy")
            ).asJava, underlay.formatter)
            info(report.toString)
        }


        {
            val report = underlay.reporter.generate(time,List(
                underlay.formatNum("K1", 12),
                underlay.formatString("K2", "Happy"),
                underlay.formatNum("K3", 12),
                underlay.formatString("K4", "Happy"),
                underlay.formatNum("K5", 12),
                underlay.formatString("K6", "Happy")
            ).asJava, underlay.formatter)
            info(report.toString)
        }




    }

}
