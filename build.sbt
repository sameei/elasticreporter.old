
// ============================================================

val appVersion = "0.5.1-SNAPSHOT"

lazy val common = Seq(
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12", "2.12.6"),
    organization := "xyz.sigmalab.xtool",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    libraryDependencies ++= Seq(
        "circe-core", "circe-generic", "circe-parser"
    ).map { m => "io.circe" %% m % "0.9.3" % Test },
    scalacOptions ++= Seq(
        "-deprecation",
        "-feature"
    )
)

def define(moduleName : String, artifact : String, dirName : String) = {
    Project(moduleName, file(s"module/${dirName}"))
        .settings(
            name := artifact
        ).settings(common : _*)
}

def flink(moduleName : String, flinkVersion : String, v: String) = {
    define(
        moduleName,
        s"elasticreporter-flink${flinkVersion}",
        moduleName
    ).settings(
        sbt.Keys.version := v,
        crossScalaVersions := Seq("2.11.12"),
        libraryDependencies += "org.apache.flink" % "flink-metrics-core" % flinkVersion % Provided,
        libraryDependencies += "org.apache.flink" % "flink-core" % flinkVersion % Provided,
        libraryDependencies += "org.apache.flink" %% "flink-runtime" % flinkVersion % Provided,
        // libraryDependencies += "xmlenc" % "xmlenc" % "0.52" % Provided,
        assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
    )
}

// ============================================================

lazy val cmn = define(
    "common",
    "elasticreporter-common",
    "common"
).settings(
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25" % Provided,
    sbt.Keys.version := appVersion
)

lazy val flink14 = flink("flink14", "1.4.2", appVersion).dependsOn(cmn)

lazy val flink12 = flink("flink12", "1.2.1", appVersion)
    .dependsOn(cmn)
    .settings(
        sourceDirectory := (sourceDirectory in flink14).value,
    )

lazy val kamon = define("kamon", "elasticreporter-kamon", "kamon")
    .settings(
        libraryDependencies += "io.kamon" %% "kamon-core" % "1.1.0" % Provided,
        sbt.Keys.version := appVersion
    ).dependsOn(cmn)

lazy val examplejob = {

    val flinkDependencies = Seq(
        "flink-scala",
        "flink-streaming-scala"
    ) map { m =>
        "org.apache.flink" %% m % "1.4.2" % Provided
    }

    val flinkJob = taskKey[File]("Source of jar")

    val flinkMain = taskKey[String]("Job Main Class")

    val flinkHome = taskKey[String]("Path to flink-home directory")

    val flinkMaster = taskKey[String]("Host & Port to Flink JobManager")

    val flinkRun = taskKey[Unit]("Run by flink")

    define("examplejob", "examplejob", "examplejob")
        .settings(
            assembly / mainClass := Some("org.example.Job"),
            libraryDependencies ++= flinkDependencies,
            libraryDependencies += "org.apache.flink" % "flink-metrics-dropwizard" % "1.4.2" % Provided,
            Compile / run := Defaults.runTask(Compile / fullClasspath,
                Compile / run / mainClass,
                Compile / run / runner
            ).evaluated,
            Compile / run / fork := true,
            Global / cancelable := true,
            assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false),
            flinkJob := assembly.value,
            flinkHome := "/home/reza/mine/opt-hdd/flink-1.4.2/",
            flinkMaster := "jobmanager:6123",
            flinkMain := "org.example.StreamTest",
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

lazy val root = (project in file(".")).aggregate(cmn, flink14, flink12, examplejob)
