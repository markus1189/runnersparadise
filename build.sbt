lazy val libDeps = {
  val http4sVersion  = "0.15.3"
  val circeVersion   = "0.6.1"
  val phantomVersion = "2.1.3"
  val logbackVersion = "1.2.1"

  Seq(
    "org.http4s"     %% "http4s-dsl"          % http4sVersion,
    "org.http4s"     %% "http4s-blaze-server" % http4sVersion,
    "org.http4s"     %% "http4s-blaze-client" % http4sVersion,
    "org.http4s"     %% "http4s-circe"        % http4sVersion,
    "io.circe"       %% "circe-generic"       % circeVersion,
    "com.outworkers" %% "phantom-dsl"         % phantomVersion,
    "ch.qos.logback" % "logback-classic"      % logbackVersion
  )
}

lazy val testDeps = {
  val scalatestVersion     = "3.0.1"
  val scalaCheckVersion    = "1.13.4"
  val cassandraUnitVersion = "3.1.3.2"

  Seq(
    "org.scalatest"     %% "scalatest"     % scalatestVersion     % "test",
    "org.scalacheck"    %% "scalacheck"    % scalaCheckVersion    % "test",
    "org.cassandraunit" % "cassandra-unit" % cassandraUnitVersion % "test"
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
    fork := true,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    libraryDependencies ++= libDeps,
    libraryDependencies ++= testDeps
  )
