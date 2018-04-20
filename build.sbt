
// ============================================================

lazy val common = Seq(
  scalaVersion := "2.11.11"
)

def define(moduleName: String, artifact: String, dirName: String) = {
  Project(moduleName, file(s"module/${dirName}"))
    .settings(
      name := artifact
    ).settings(common:_*)
}

// ============================================================

lazy val xjava = define("xjava", "xjava", "xjava")

lazy val base = define(
  "base",
  "elasticsearch-metric-reporter-base",
  "base"
).settings(
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
).dependsOn(xjava)

lazy val flink14 = define(
  "flink142",
  "elasticsearch-metric-reporter-flink-1.4",
  "flink14"
).settings(
  libraryDependencies += "org.apache.flink" % "flink-metrics-core" % "1.4.2"
).dependsOn(base)

// ============================================================