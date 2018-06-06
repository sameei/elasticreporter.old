import sbt._
import sbt.Keys._

object FlinkRemote extends AutoPlugin {

    object autoImport {

        val flinkJob = taskKey[File]("Source of jar")

        val flinkMain = taskKey[String]("Job Main Class")

        val flinkHome = taskKey[String]("Path to flink-home directory")

        val flinkMaster = taskKey[String]("Host & Port to Flink JobManager")

        val flinkRun = taskKey[Unit]("Run by flink")
    }

    import autoImport._

    override def trigger = allRequirements

    override def requires = sbt.plugins.JvmPlugin

    override def projectSettings = Seq(
        flinkJob := null,
        flinkHome := null,
        flinkMaster := null,
        flinkMain := null,
        flinkRun := {
            val jar = flinkJob.value
            val home = flinkHome.value
            val master = flinkMaster.value
            val main = flinkMain.value
            val logger = streams.value.log("FlinkRemote")

            logger.info(s"${home}/bin/flink run -c ${main} -m ${master} ${jar}")
        }
    )

}
