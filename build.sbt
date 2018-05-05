
// ============================================================

lazy val common = Seq(
  scalaVersion := "2.11.11",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

def define(moduleName: String, artifact: String, dirName: String) = {
  Project(moduleName, file(s"module/${dirName}"))
    .settings(
      name := artifact
    ).settings(common:_*)
}

def flink(moduleName: String, flinkVersion: String) = {
  define(
    moduleName,
    s"elasticsearch-metric-reporter-flink-${flinkVersion}",
    moduleName
  ).settings(
    libraryDependencies += "org.apache.flink" % "flink-metrics-core" % flinkVersion % Provided,
    libraryDependencies += "org.apache.flink" % "flink-core" % flinkVersion % Provided,
    libraryDependencies += "org.apache.flink" %% "flink-runtime" % flinkVersion % Provided,
    // libraryDependencies += "xmlenc" % "xmlenc" % "0.52" % Provided,
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
    assemblyJarName in assembly := s"es-reporter-flink-${flinkVersion}.jar"
  ).dependsOn(base)
}

// ============================================================

lazy val xjava = define("xjava", "xjava", "xjava")

lazy val base = define(
  "base",
  "elasticsearch-metric-reporter-base",
  "base"
).settings(
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25" % Provided
).dependsOn(xjava)

lazy val flink14 = flink("flink14", "1.4.2")

lazy val flink12 = flink("flink12", "1.2.1")
      .settings(
        sourceDirectory := (sourceDirectory in flink14).value,
      )

lazy val newgen = define(
  "newgen",
  "esreporter-common",
  "newgen"
).settings(
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25" % Provided
).dependsOn(xjava)

lazy val newgenflink14 = flink("ngflink", "1.4.2").dependsOn(newgen)

// ============================================================