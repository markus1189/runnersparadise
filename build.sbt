lazy val libDeps = {
  val http4sVersion    = "0.15.3"
  val circeVersion     = "0.6.1"
  val cassandraVersion = "3.1.3"

  Seq(
    "org.http4s"             %% "http4s-dsl"           % http4sVersion,
    "org.http4s"             %% "http4s-blaze-server"  % http4sVersion,
    "org.http4s"             %% "http4s-blaze-client"  % http4sVersion,
    "org.http4s"             %% "http4s-circe"         % http4sVersion,
    "io.circe"               %% "circe-generic"        % circeVersion,
    "com.datastax.cassandra" % "cassandra-driver-core" % cassandraVersion
  )
}

lazy val testDeps = {
  val scalatestVersion = "3.0.1"

  Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  )

}

lazy val root = (project in file("."))
  .settings(
    name := "encodings",
    scalaVersion := "2.12.1",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-unused",
      "-Ywarn-value-discard",
      "-Ypartial-unification",
      "-language:implicitConversions",
      "-language:higherKinds"
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    libraryDependencies ++= libDeps,
    libraryDependencies ++= testDeps
  )
